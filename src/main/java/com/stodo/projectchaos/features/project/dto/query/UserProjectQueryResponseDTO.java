package com.stodo.projectchaos.features.project.dto.query;

import com.stodo.projectchaos.model.enums.ProjectRoleEnum;

import java.time.Instant;
import java.util.UUID;

public record UserProjectQueryResponseDTO(
        UUID projectId,
        String projectName,
        String projectDescription,
        Instant projectCreatedDate,
        ProjectRoleEnum projectRole,
        Instant projectJoinedDate
) {}
