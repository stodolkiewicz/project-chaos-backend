package com.stodo.projectchaos.features.user.dto.service;

import java.util.UUID;

public record AssignUserToProject (
        UUID projectId,
        UUID userId,
        String projectRole
) {}
