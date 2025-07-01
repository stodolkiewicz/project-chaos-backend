package com.stodo.projectchaos.model.dto.project.list.query;

import java.util.UUID;

public record SimpleProjectQueryResponseDTO(
        UUID projectId,
        String projectName
) {} 