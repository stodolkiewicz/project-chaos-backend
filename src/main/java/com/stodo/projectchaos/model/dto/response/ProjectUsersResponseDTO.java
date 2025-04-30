package com.stodo.projectchaos.model.dto.response;

import java.util.List;

public record ProjectUsersResponseDTO(List<ProjectUserQueryResponseDTO> projectUsers) {
}
