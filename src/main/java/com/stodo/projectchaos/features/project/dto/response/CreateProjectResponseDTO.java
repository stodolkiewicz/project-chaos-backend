package com.stodo.projectchaos.features.project.dto.response;

import java.util.UUID;

public record CreateProjectResponseDTO(UUID projectId, String name) {
}