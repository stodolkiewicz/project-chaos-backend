package com.stodo.projectchaos.ai.chat;

import com.stodo.projectchaos.ai.conversation.AIConversationService;
import com.stodo.projectchaos.ai.conversation.dto.service.AIConversation;
import com.stodo.projectchaos.ai.usage.AIUsageLogsService;
import com.stodo.projectchaos.exception.TooManyAIRequestsException;
import com.stodo.projectchaos.features.user.UserService;
import com.stodo.projectchaos.model.entity.AIConversationEntity;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class AIChatService {
    private final ChatClient chatClient;
    private final AIUsageLogsService aiUsageLogsService;
    private final UserService userService;
    private final AIConversationService aiConversationService;
    private final ChatMemory messageWindowChatMemory;

    public AIChatService(ChatClient chatClient, AIUsageLogsService aiUsageLogsService, UserService userService, AIConversationService aiConversationService, ChatMemory messageWindowChatMemory) {
        this.chatClient = chatClient;
        this.aiUsageLogsService = aiUsageLogsService;
        this.userService = userService;
        this.aiConversationService = aiConversationService;
        this.messageWindowChatMemory = messageWindowChatMemory;
    }

    @Value("${app.ai.number-of-requests-in-time-window-limit:10}")
    int numberOfRequestsInTimeWindowLimit;

    @Value("${app.ai.time-window-in-minutes:60}")
    int timeWindowInMinutes;

    public Flux<String> chat(String question, String conversationId, UUID userId, boolean conversationHasTitle) {
        if(aiUsageLogsService.isNumberOfRequestsInTimeWindowReached(
                userId, numberOfRequestsInTimeWindowLimit, timeWindowInMinutes))
        {
            throw new TooManyAIRequestsException(timeWindowInMinutes, numberOfRequestsInTimeWindowLimit);
        }

        long start = System.currentTimeMillis();

        AtomicReference<ChatResponse> finalResponse = new AtomicReference<>();

        return chatClient.prompt()
                .system("You are helpful adviser. Your answers are cheerful, but rather on the concise side.")
                .user(question)
                .advisors(advisorSpec -> advisorSpec
                        .advisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                        .param(CONVERSATION_ID, conversationId)
                )
                .stream()
                .chatResponse()
                // save last response
                .doOnNext(finalResponse::set)

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

    public Flux<String> createConversationAndChat(UUID projectId, UUID userId, String conversationId, String question) {
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

        return chat(question, conversationId, userId, conversation.conversationHasTitle());
    }

    public void createAndSaveConversationTitle(String conversationId, String question) {
        String title = chatClient.prompt()
                .system("Generate a short, maximum 5-word title for this chat based on the user question. Return only the title text.")
                .user(question)
                .call()
                .content();
        aiConversationService.updateConversationTitle(conversationId, title);
    }

}
