package com.stodo.projectchaos.ai.conversation.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponseDTO(
        UUID id,
        UUID projectId,
        String title,
        Instant createdAt
) {
}