package com.stodo.projectchaos.model.dto.invitation.list.response;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponseDTO(
    UUID id,
    String email,
    String role,
    UUID projectId,
    String projectName,
    String invitedByEmail,
    Instant createdDate
) {
}