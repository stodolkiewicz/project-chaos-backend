package com.stodo.projectchaos.features.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UnassignUserFromProjectRequestDTO(
        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        String userEmail
) {
}