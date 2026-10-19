package com.stodo.projectchaos.features.column.dto.request;

import com.stodo.projectchaos.model.enums.ColumnSoundEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateColumnPreferenceRequestDTO(
    @NotNull
    UUID columnId,
    @NotNull
    ColumnSoundEnum sound,
    boolean confetti
) {}
