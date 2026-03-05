package com.stodo.projectchaos.features.invitation.dto.service;

import com.stodo.projectchaos.features.invitation.dto.response.InvitationStatus;

import java.util.UUID;

public record CreateInvitation (
        UUID id,
        String email,
        String role,
        InvitationStatus invitationStatus
) {
}
