package com.stodo.projectchaos.features.projectuser.dto.service;

import java.util.UUID;

public record AssignUserToProject (
        UUID projectId,
        UUID userId,
        String projectRole
) {}
