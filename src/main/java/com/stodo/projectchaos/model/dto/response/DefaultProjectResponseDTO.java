package com.stodo.projectchaos.model.dto.response;

import com.stodo.projectchaos.model.entity.ProjectEntity;

import java.time.Instant;
import java.util.UUID;

public record DefaultProjectResponseDTO(
    UUID projectId,
    String projectName,
    boolean hasDefaultProject,
    Instant createdDate,
    Instant lastModifiedDate,
    String lastModifiedBy
) {
    public static DefaultProjectResponseDTO empty() {
        return new DefaultProjectResponseDTO(
            null, 
            null, 
            false,
            null,
            null,
            null
        );
    }

    public static DefaultProjectResponseDTO of(ProjectEntity project) {
        return new DefaultProjectResponseDTO(
            project.getId(),
            project.getName(),
            true,
            project.getCreatedDate(),
            project.getLastModifiedDate(),
            project.getLastModifiedBy()
        );
    }
}
