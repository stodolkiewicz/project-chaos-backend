package com.stodo.projectchaos.ai.chatmemory.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ChatMemoryResponseDTO(
        UUID id,
        String conversationId,
        String content,
        MessageType type,
        Instant timestamp
) {
    public enum MessageType {
        USER, ASSISTANT, SYSTEM, TOOL
    }
}