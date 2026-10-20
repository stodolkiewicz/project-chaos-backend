package com.stodo.projectchaos.features.priority.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTaskPriorityRequestDTO(
        // Null means a new priority
        UUID id,
        @NotBlank
        @Size(max = 50)
        String name,
        @NotNull
        @Min(1)
        @Max(5)
        Short priorityValue,
        @NotNull
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
        String color
) {
}
