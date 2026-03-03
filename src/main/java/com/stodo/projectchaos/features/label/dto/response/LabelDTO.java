package com.stodo.projectchaos.features.label.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LabelDTO(
        UUID id,
        String name,
        String color
) {}
