package com.stodo.projectchaos.features.taskcomments.dto.service;

import java.time.Instant;
import java.util.UUID;

public record TaskComment(
        UUID id,
        UUID taskId,
        UUID authorId,
        String content,
        UUID replyToId,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
}