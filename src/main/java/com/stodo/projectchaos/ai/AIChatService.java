package com.stodo.projectchaos.ai;

import com.stodo.projectchaos.exception.TooManyAIRequestsException;
import com.stodo.projectchaos.features.user.UserService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Value;
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

    public AIChatService(ChatClient chatClient, AIUsageLogsService aiUsageLogsService, UserService userService) {
        this.chatClient = chatClient;
        this.aiUsageLogsService = aiUsageLogsService;
        this.userService = userService;
    }

    @Value("${app.ai.number-of-requests-in-time-window-limit:10}")
    int numberOfRequestsInTimeWindowLimit;

    @Value("${app.ai.time-window-in-minutes:60}")
    int timeWindowInMinutes;

    public Flux<String> talk(String question, UUID conversationId, String userEmail) {
        UUID userId = userService.getUserIdByEmail(userEmail);
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
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, conversationId))
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
                })

                .map(response -> Optional.ofNullable(response.getResult())
                    .map(Generation::getOutput)
                    .map(AssistantMessage::getText)
                    .orElse("")
                );
    }

}
