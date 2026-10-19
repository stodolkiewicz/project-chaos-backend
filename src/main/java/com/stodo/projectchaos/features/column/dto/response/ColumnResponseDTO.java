package com.stodo.projectchaos.features.column.dto.response;

import com.stodo.projectchaos.model.enums.ColumnColorEnum;

public record ColumnResponseDTO(
    String id,
    String name,
    Short position,
    ColumnColorEnum color
) {}