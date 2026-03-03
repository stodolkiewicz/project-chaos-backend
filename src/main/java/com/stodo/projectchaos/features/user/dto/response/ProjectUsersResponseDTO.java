package com.stodo.projectchaos.features.user.dto.response;

import com.stodo.projectchaos.features.user.dto.query.ProjectUserQueryResponseDTO;

import java.util.List;

public record ProjectUsersResponseDTO(List<ProjectUserQueryResponseDTO> projectUsers) {
}