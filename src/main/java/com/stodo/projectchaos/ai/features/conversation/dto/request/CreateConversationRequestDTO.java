package com.stodo.projectchaos.ai.features.conversation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateConversationRequestDTO(
        @NotNull UUID projectId,
        @Size(max = 255) String title
) {
}