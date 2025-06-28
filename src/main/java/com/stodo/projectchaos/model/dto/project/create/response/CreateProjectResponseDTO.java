package com.stodo.projectchaos.model.dto.project.create.response;

import java.util.UUID;

public record CreateProjectResponseDTO(UUID projectId, String name) {
}
