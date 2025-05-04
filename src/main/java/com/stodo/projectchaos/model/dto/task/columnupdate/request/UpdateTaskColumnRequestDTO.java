package com.stodo.projectchaos.model.dto.task.columnupdate.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateTaskColumnRequestDTO (
        @NotNull
        UUID targetColumnId,
        @NotNull
        Double positionInColumn,
        @NotNull
        @Size(max = 2)
        List<Double> nearestNeighboursPositionInColumn) {
}
