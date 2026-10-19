package com.stodo.projectchaos.features.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// The order of the columns sets their order on the board.
// Existing columns missing from the list are deleted.
public record UpdateProjectRequestDTO(
        @Size(max = 5000)
        String description,
        @NotNull
        @Size(min = 2)
        @Valid
        List<UpdateColumnRequestDTO> columns
) {
}
