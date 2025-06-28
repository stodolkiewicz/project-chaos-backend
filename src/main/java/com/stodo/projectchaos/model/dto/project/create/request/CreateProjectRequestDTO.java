package com.stodo.projectchaos.model.dto.project.create.request;

import java.util.List;

public record CreateProjectRequestDTO(String name, String description, List<String> columns) {
}

