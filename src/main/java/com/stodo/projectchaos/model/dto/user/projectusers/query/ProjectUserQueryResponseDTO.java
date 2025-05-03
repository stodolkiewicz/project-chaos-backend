package com.stodo.projectchaos.model.dto.user.projectusers.query;

public record ProjectUserQueryResponseDTO(String email) {
    public ProjectUserQueryResponseDTO {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
    }
}
