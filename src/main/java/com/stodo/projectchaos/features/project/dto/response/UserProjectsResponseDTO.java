package com.stodo.projectchaos.features.project.dto.response;

import com.stodo.projectchaos.features.project.dto.query.UserProjectQueryResponseDTO;

import java.util.List;

public record UserProjectsResponseDTO(List<UserProjectQueryResponseDTO> projects) {
}