package com.stodo.projectchaos.model.dto.label.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LabelDTO(
        UUID id,
        String name,
        String color
) {}
