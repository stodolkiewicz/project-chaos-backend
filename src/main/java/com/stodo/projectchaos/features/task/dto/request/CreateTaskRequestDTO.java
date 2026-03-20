package com.stodo.projectchaos.features.task.dto.request;

import com.stodo.projectchaos.model.enums.TaskStageEnum;
import jakarta.validation.Valid;
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
        @Valid
        List<LabelDTO> labels,
        TaskStageEnum stage
        ) {
}
