package com.stodo.projectchaos.features.taskcomments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTaskCommentRequestDTO(
        @NotNull UUID taskId,
        @NotBlank String content,
        UUID replyToId
) {
}