package com.stodo.projectchaos.ai.conversation.dto.service;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AIConversation(
        String id,
        UUID projectId,
        UUID userId,
        String title,
        Boolean conversationHasTitle,
        Instant createdAt
) {
}