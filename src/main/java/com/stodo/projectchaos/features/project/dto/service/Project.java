package com.stodo.projectchaos.features.project.dto.service;

import java.time.Instant;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        String description,
        Instant createdDate
) {
}