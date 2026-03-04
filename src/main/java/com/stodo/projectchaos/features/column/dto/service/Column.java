package com.stodo.projectchaos.features.column.dto.service;

import java.util.UUID;

public record Column(
    UUID id,
    String name,
    Short position
) {}