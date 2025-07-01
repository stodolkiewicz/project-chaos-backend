package com.stodo.projectchaos.model.dto.project.list.response;

import com.stodo.projectchaos.model.dto.project.list.query.SimpleProjectQueryResponseDTO;

import java.util.List;

public record SimpleProjectsResponseDTO(List<SimpleProjectQueryResponseDTO> projects) {
} 