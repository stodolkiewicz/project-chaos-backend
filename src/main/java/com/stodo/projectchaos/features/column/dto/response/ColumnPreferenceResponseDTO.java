package com.stodo.projectchaos.features.column.dto.response;

import com.stodo.projectchaos.model.enums.ColumnSoundEnum;

public record ColumnPreferenceResponseDTO(
    String columnId,
    ColumnSoundEnum sound,
    boolean confetti
) {}
