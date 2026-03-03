package com.stodo.projectchaos.features.invitation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateInvitationRequestDTO(
    @Email
    @NotBlank
    String email,
    
    @NotBlank
    String role
) {
}