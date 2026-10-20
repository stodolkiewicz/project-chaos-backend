package com.stodo.projectchaos.features.priority.dto.service;

import java.util.UUID;

public record TaskPriority(
    // Null means a priority that is not saved yet
    UUID id,
    Short priorityValue,
    String name,
    String color,
    // The number of tasks with this priority, in every stage
    long taskCount
) {}
