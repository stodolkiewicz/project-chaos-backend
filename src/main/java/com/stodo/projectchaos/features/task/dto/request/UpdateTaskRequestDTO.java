package com.stodo.projectchaos.features.task.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record UpdateTaskRequestDTO(
        @NotBlank
        String title,
        String description,
        // Null or blank removes the assignee
        String assigneeEmail,
        // Null removes the priority
        UUID priorityId,
        @Valid
        List<LabelDTO> labels
) {
}
