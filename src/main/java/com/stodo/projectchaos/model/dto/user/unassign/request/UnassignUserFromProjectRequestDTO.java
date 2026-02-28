package com.stodo.projectchaos.model.dto.user.unassign.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UnassignUserFromProjectRequestDTO(
        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        String userEmail
) {
}