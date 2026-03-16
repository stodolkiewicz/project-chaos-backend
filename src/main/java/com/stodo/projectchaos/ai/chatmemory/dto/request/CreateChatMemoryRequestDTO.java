package com.stodo.projectchaos.ai.chatmemory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChatMemoryRequestDTO(
        @NotBlank String conversationId,
        @NotBlank String content,
        @NotNull MessageType type
) {
    public enum MessageType {
        USER, ASSISTANT, SYSTEM, TOOL
    }
}