package com.stodo.projectchaos.features.projectuser.dto.response;

import java.util.UUID;

public record AssignUserToProjectResponseDTO(
    UUID projectId,
    UUID userId,
    String projectRole
) {} 