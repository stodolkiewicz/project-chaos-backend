package com.stodo.projectchaos.model.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record CreateTaskRequestDTO (
        @NotNull
        String title,
        String description,
        @NotNull
        Double positionInColumn,
        @NotNull
        String assigneeEmail,
        // no columnId -> Task should be created in project_backlog
        UUID columnId,
        UUID priorityId,
        Set<String> labels
        ) {
}
