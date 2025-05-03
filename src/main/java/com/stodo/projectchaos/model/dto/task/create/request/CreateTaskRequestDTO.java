package com.stodo.projectchaos.model.dto.task.create.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
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
        List<LabelDTO> labels
        ) {
}
