package com.stodo.projectchaos.features.vectorizationoutbox.dto.service;

import com.stodo.projectchaos.model.enums.VectorizationOutboxStatusEnum;

import java.time.Instant;
import java.util.UUID;

public record VectorizationOutbox(
        UUID id,
        UUID attachmentId,
        VectorizationOutboxStatusEnum status,
        Integer retryCount,
        Instant createdDate,
        Instant processedAt
) {
}
