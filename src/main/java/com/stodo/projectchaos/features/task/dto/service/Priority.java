package com.stodo.projectchaos.features.task.dto.service;

import java.util.UUID;

public record Priority(UUID id, Short priorityValue, String name, String color) {}