package com.stodo.projectchaos.model.dto.label.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record LabelResponseDTO(
        UUID projectId,
        List<LabelDTO> labels
) {

}