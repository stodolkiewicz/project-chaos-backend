package com.stodo.projectchaos.features.project.dto.request;

import com.stodo.projectchaos.model.enums.ColumnColorEnum;

// A null color means the first palette color not used yet by the other columns
public record CreateColumnRequestDTO(String name, ColumnColorEnum color) {
}
