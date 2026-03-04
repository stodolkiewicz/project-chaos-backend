package com.stodo.projectchaos.features.task.dto.service;

import java.time.Instant;
import java.util.UUID;

public record Task(
        UUID id,
        String title,
        String description,
        Double positionInColumn,
        UUID columnId,
        UUID assigneeId,
        UUID priorityId,
        Instant createdDate
) {
}