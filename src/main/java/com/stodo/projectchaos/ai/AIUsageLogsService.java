package com.stodo.projectchaos.ai;

import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.AIUsageLogsEntity;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AIUsageLogsService {

    private final AIUsageLogsRepository repository;
    private final UserRepository userRepository;

    public AIUsageLogsService(AIUsageLogsRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
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

        repository.save(aiUsageLogsEntity);
    }
}
