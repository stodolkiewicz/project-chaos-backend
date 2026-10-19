package com.stodo.projectchaos.features.column.dto.service;

import com.stodo.projectchaos.model.enums.ColumnColorEnum;

import java.util.UUID;

public record Column(
    UUID id,
    String name,
    Short position,
    ColumnColorEnum color
) {}