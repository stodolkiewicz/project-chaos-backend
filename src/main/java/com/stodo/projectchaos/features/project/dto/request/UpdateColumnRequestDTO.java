package com.stodo.projectchaos.features.project.dto.request;

import com.stodo.projectchaos.model.enums.ColumnColorEnum;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateColumnRequestDTO(
        // Null means a new column
        UUID id,
        @NotBlank
        String name,
        // Only a new column uses it. Null means the first palette color not used yet by the other columns
        ColumnColorEnum color
) {
}
