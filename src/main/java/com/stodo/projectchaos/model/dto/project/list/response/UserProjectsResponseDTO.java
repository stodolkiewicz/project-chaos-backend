package com.stodo.projectchaos.model.dto.project.list.response;

import com.stodo.projectchaos.model.dto.project.list.query.UserProjectQueryResponseDTO;

import java.util.List;
import java.util.UUID;

public record UserProjectsResponseDTO(List<UserProjectQueryResponseDTO> projects, UUID defaultProjectId) {
}
