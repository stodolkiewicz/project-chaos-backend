package com.stodo.projectchaos.features.invitation.dto.service;

import java.time.Instant;
import java.util.UUID;

public record Invitation(
        UUID id,
        String email,
        String role,
        UUID projectId,
        String projectName,
        UUID invitedById,
        String invitedByEmail,
        Instant createdDate
) {
}