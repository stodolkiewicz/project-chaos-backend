package com.stodo.projectchaos.features.column.dto.service;

import com.stodo.projectchaos.model.enums.ColumnSoundEnum;

import java.util.UUID;

public record ColumnPreference(
    UUID columnId,
    ColumnSoundEnum sound,
    boolean confetti
) {}
