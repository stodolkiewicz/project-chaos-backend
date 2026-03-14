package com.stodo.projectchaos.ai;

import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.AIUsageLogsEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AIUsageLogsService {
    private final AIUsageLogsRepository aiUsageLogsRepository;
    private final UserRepository userRepository;

    public AIUsageLogsService(AIUsageLogsRepository aiUsageLogsRepository, UserRepository userRepository) {
        this.aiUsageLogsRepository = aiUsageLogsRepository;
        this.userRepository = userRepository;
    }

    public boolean isNumberOfRequestsInTimeWindowReached(
            UUID userId,
            int numberOfRequestsInTimeWindowLimit,
            int timeWindowInMinutes
    ) {
        Instant timeWindowStartDate = Instant.now().minus(timeWindowInMinutes, ChronoUnit.MINUTES);
        return aiUsageLogsRepository.isNumberOfRequestsInTimeWindowLimitReached(
                userId,
                numberOfRequestsInTimeWindowLimit,
                timeWindowStartDate
            );
    }

    @Transactional
    @Async
    public void saveUsage(UUID userId, UUID conversationId, String modelId,
                          Integer promptTokens, Integer completionTokens,
                          String requestId, Long latencyMs) {
        AIUsageLogsEntity aiUsageLogsEntity = AIUsageLogsEntity.builder()
                .user(userRepository.getReferenceById(userId))
                .conversationId(conversationId)
                .modelId(modelId)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .requestId(requestId)
                .latencyMs(latencyMs)
                .build();

        aiUsageLogsRepository.save(aiUsageLogsEntity);
    }
}
