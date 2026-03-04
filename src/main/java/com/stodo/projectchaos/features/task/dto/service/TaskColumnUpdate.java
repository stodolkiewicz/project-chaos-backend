package com.stodo.projectchaos.features.task.dto.service;

import java.util.UUID;

public record TaskColumnUpdate(
        UUID taskId,
        UUID columnId,
        Double positionInColumn
) {
}