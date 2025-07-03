package com.stodo.projectchaos.model.dto.project.assignuser.response;

import java.util.UUID;

public record AssignUserToProjectResponseDTO(
    UUID projectId,
    String userEmail,
    String projectRole,
    String message
) {} 