package com.stodo.projectchaos.features.task.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MoveTasksRequestDTO(
        @NotNull
        @NotEmpty
        List<UUID> taskIds
) {
}