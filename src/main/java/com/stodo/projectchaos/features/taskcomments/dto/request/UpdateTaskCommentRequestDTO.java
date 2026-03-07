package com.stodo.projectchaos.features.taskcomments.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskCommentRequestDTO(
        @NotBlank String content
) {
}