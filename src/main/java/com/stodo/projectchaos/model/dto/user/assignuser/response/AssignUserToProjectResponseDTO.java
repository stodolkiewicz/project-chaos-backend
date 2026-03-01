package com.stodo.projectchaos.model.dto.user.assignuser.response;

import java.util.UUID;

public record AssignUserToProjectResponseDTO(
    UUID projectId,
    UUID userId,
    String projectRole
) {} 