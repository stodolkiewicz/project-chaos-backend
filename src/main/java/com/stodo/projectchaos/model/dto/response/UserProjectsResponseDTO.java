package com.stodo.projectchaos.model.dto.response;

import java.util.List;
import java.util.UUID;

public record UserProjectsResponseDTO(List<UserProjectQueryResponseDTO> projects, UUID defaultProjectId) {
}
