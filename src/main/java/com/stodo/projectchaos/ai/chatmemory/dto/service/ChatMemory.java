package com.stodo.projectchaos.ai.chatmemory.dto.service;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ChatMemory(
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