package com.stodo.projectchaos.features.task.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UpdateTaskColumnResponseDTO(
    UUID id,
    String title,
    String description,
    Double positionInColumn,
    String assigneeEmail,
    UUID columnId,
    UUID priorityId,
    Instant createdDate,
    Instant lastModifiedDate,
    String lastModifiedBy
) {
}