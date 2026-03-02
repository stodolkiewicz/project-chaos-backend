package com.stodo.projectchaos.model.dto.invitation.create.response;

import java.util.UUID;

public record CreateInvitationResponseDTO(
    UUID id,
    String email,
    String role,
    UUID projectId
) {
}