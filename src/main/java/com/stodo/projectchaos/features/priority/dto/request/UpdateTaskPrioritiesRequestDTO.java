package com.stodo.projectchaos.features.priority.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Existing priorities missing from the list are deleted, and their tasks lose the priority.
// Values are unique within a project and go from 1 to 5, so the list has at most 5 priorities.
public record UpdateTaskPrioritiesRequestDTO(
        @NotNull
        @Size(max = 5)
        @Valid
        List<UpdateTaskPriorityRequestDTO> priorities
) {
}
