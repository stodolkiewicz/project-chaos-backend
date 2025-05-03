package com.stodo.projectchaos.model.dto.project.byid.response;


import java.time.Instant;
import java.util.UUID;

public record ProjectResponseDTO (
    UUID id,
    String name,
    String description,
    Instant createdDate,
    Instant lastModifiedDate,
    String lastModifiedBy
) {
}
