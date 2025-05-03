package com.stodo.projectchaos.model.dto.user.projectusers.response;

import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;

import java.util.List;

public record ProjectUsersResponseDTO(List<ProjectUserQueryResponseDTO> projectUsers) {
}
