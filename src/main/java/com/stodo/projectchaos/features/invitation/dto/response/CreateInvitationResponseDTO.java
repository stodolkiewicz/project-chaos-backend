package com.stodo.projectchaos.features.invitation.dto.response;

import java.util.UUID;

public record CreateInvitationResponseDTO(
    UUID id,
    String email,
    String role,
    InvitationStatus invitationStatus
) {
}