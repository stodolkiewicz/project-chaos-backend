package com.stodo.projectchaos.features.project.dto.query;

import java.util.UUID;

public record UserAlternativeProjectQueryResponseDTO(String email, UUID projectId) {
}