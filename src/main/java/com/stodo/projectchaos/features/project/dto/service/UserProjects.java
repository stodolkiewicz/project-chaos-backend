package com.stodo.projectchaos.features.project.dto.service;

import com.stodo.projectchaos.features.project.dto.query.UserProjectQueryResponseDTO;
import java.util.List;

public record UserProjects(
        List<UserProjectQueryResponseDTO> projects
) {
}