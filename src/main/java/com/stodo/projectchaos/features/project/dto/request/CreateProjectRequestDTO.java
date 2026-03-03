package com.stodo.projectchaos.features.project.dto.request;

import java.util.List;

public record CreateProjectRequestDTO(String name, String description, List<String> columns) {
}

