package com.stodo.projectchaos.features.priority.dto.service;

import java.util.UUID;

public record TaskPriority(
    UUID id,
    Short priorityValue,
    String name,
    String color
) {}