package com.stodo.projectchaos.ai.chat;

import com.stodo.projectchaos.ai.features.conversation.AIConversationService;
import com.stodo.projectchaos.ai.features.conversation.dto.service.AIConversation;
import com.stodo.projectchaos.ai.features.prompt.LangfusePromptService;
import com.stodo.projectchaos.ai.features.prompt.dto.service.SystemPrompt;
import com.stodo.projectchaos.ai.features.usage.AIUsageLogsService;
import com.stodo.projectchaos.ai.tools.TaskProposalTools;
import com.stodo.projectchaos.config.ChatClientSessionUserObservationFilter;
import com.stodo.projectchaos.config.ChatModelPromptLinkObservationFilter;
import com.stodo.projectchaos.exception.TooManyAIRequestsException;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.project.dto.service.Project;
import com.stodo.projectchaos.features.user.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@Service
public class AIChatService {
    private final ChatClient chatClient;
    private final ChatClient simpleChatClient;
    private final AIUsageLogsService aiUsageLogsService;
    private final UserService userService;
    private final AIConversationService aiConversationService;
    private final ChatMemory messageWindowChatMemory;
    private final ProjectService projectService;
    private final MeterRegistry registry;
    private final LangfusePromptService langfusePromptService;

    public AIChatService(ChatClient chatClient, ChatClient simpleChatClient, AIUsageLogsService aiUsageLogsService, UserService userService, AIConversationService aiConversationService, ChatMemory messageWindowChatMemory, ProjectService projectService, MeterRegistry registry, LangfusePromptService langfusePromptService) {
        this.chatClient = chatClient;
        this.simpleChatClient = simpleChatClient;
        this.aiUsageLogsService = aiUsageLogsService;
        this.userService = userService;
        this.aiConversationService = aiConversationService;
        this.messageWindowChatMemory = messageWindowChatMemory;
        this.projectService = projectService;
        this.registry = registry;
        this.langfusePromptService = langfusePromptService;
    }

    @Value("${app.ai.number-of-requests-in-time-window-limit:10}")
    int numberOfRequestsInTimeWindowLimit;

    @Value("${app.ai.time-window-in-minutes:60}")
    int timeWindowInMinutes;

    public Flux<String> chat(String question, UUID projectId, UUID userId, String conversationId, boolean conversationHasTitle, Sinks.Many<ServerSentEvent<Object>> events) {
        if(aiUsageLogsService.isNumberOfRequestsInTimeWindowReached(
                userId, numberOfRequestsInTimeWindowLimit, timeWindowInMinutes))
        {
            log.warn("AI request limit reached [userId={}, projectId={}, limit={}, windowMinutes={}]",
                    userId,
                    projectId,
                    numberOfRequestsInTimeWindowLimit,
                    timeWindowInMinutes);

            throw new TooManyAIRequestsException(timeWindowInMinutes, numberOfRequestsInTimeWindowLimit);
        }

        long start = System.currentTimeMillis();

        Project project = projectService.findProjectById(projectId);

        AtomicReference<ChatResponse> finalResponse = new AtomicReference<>();
        AtomicBoolean firstTokenReceived = new AtomicBoolean(false);

        SystemPrompt systemPrompt = langfusePromptService.getSystemPrompt();

        return chatClient.prompt()
                .system(systemSpec -> {
                    systemSpec
                            .text(systemPrompt.text())
                            .param("projectName", project.name())
                            .param("projectId", projectId)
                            .param("projectDescription", project.description())
                            .param("createdAt", project.createdDate())
                            .param("userId", userId);

                    // The fallback prompt is not in Langfuse, so it must not be linked.
                    if (systemPrompt.isFromLangfuse()) {
                        systemSpec
                                .metadata(ChatModelPromptLinkObservationFilter.PROMPT_NAME, systemPrompt.name())
                                .metadata(ChatModelPromptLinkObservationFilter.PROMPT_VERSION, systemPrompt.version());
                    }
                })
                .user(question)
                .toolContext(Map.of(
                        TaskProposalTools.EVENTS_KEY, events,
                        TaskProposalTools.PROJECT_ID_KEY, projectId
                ))
                .advisors(advisorSpec -> advisorSpec
                        .advisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                        .param(CONVERSATION_ID, conversationId)
                        .param(ChatClientSessionUserObservationFilter.USER_ID, userId)
                )
                .stream()
                .chatResponse()
                .doOnError(error -> log.error("AI chat stream failed [userId={}, projectId={}, conversationId={}]",
                        userId,
                        projectId,
                        conversationId,
                        error))

                // save last response
                .doOnNext(response -> {
                    finalResponse.set(response);

                    if (firstTokenReceived.compareAndSet(false, true)) {
                        Timer.builder("projectchaos.ai.time_to_first_token")
                                .description("Time from the start of the call to LLM to the first token from it")
                                .publishPercentileHistogram()
                                .register(registry)
                                .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                    }
                })

                // save ai usage logs when stream ends
                .doOnComplete(() -> {
                    ChatResponse response = finalResponse.get();
                    if (response == null) return;

                    var usage = response.getMetadata().getUsage();
                    if (usage == null) return;

                    long latency = System.currentTimeMillis() - start;

                    String model = response.getMetadata().getModel();
                    String requestId = response.getMetadata().getId();
                    int promptTokens = usage.getPromptTokens();
                    int completionTokens = usage.getCompletionTokens();

                    Timer.builder("projectchaos.ai.time_to_message_completion")
                                    .description("TTime from the start of the call to LLM to the last token of the message")
                                    .publishPercentileHistogram()
                                    .register(registry)
                                    .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);

                    aiUsageLogsService.saveUsage(
                            userId,
                            conversationId,
                            model,
                            promptTokens,
                            completionTokens,
                            requestId,
                            latency
                    );

                    if(!conversationHasTitle) {
                        createAndSaveConversationTitle(conversationId, question);
                    }
                })

                .map(response -> Optional.ofNullable(response.getResult())
                    .map(Generation::getOutput)
                    .map(AssistantMessage::getText)
                    .orElse("")
                );
    }

    public Flux<String> createConversationAndChat(UUID projectId, UUID userId, String conversationId, String question, Sinks.Many<ServerSentEvent<Object>> events) {
        AIConversation conversation =
                aiConversationService.findByConversationId(conversationId)
                        .orElseGet(() ->
                                aiConversationService.createConversation(
                                        projectId,
                                        userId,
                                        conversationId,
                                        "New conversation"
                                )
                        );

        return chat(question, projectId, userId, conversationId, conversation.conversationHasTitle(), events);
    }

    public void createAndSaveConversationTitle(String conversationId, String question) {
        String title = simpleChatClient.prompt()
                .system("Generate a short, maximum 5-word title for this chat based on the user question. Return only the title text.")
                .user(question)
                .call()
                .content();
        aiConversationService.updateConversationTitle(conversationId, title);
    }

}
