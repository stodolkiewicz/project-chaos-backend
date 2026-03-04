package com.stodo.projectchaos.features.label.dto.service;

import java.util.List;
import java.util.UUID;

public record Labels(
    UUID projectId,
    List<Label> labels
) {
    public record Label(UUID id, String name, String color) {}
}