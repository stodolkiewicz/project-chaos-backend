package com.stodo.projectchaos.ai.features.conversation.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponseDTO(
        String id,
        UUID projectId,
        String title,
        Instant createdAt
) {
}