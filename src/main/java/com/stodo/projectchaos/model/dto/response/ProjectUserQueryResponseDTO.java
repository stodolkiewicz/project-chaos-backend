package com.stodo.projectchaos.model.dto.response;

public record ProjectUserQueryResponseDTO(String email) {
    public ProjectUserQueryResponseDTO {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
    }
}
