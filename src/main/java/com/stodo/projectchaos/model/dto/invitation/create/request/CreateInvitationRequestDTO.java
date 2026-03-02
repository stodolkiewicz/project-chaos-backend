package com.stodo.projectchaos.model.dto.invitation.create.request;

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