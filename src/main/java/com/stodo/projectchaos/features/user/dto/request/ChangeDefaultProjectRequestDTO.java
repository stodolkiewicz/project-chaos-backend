package com.stodo.projectchaos.features.user.dto.request;

import java.util.UUID;

public record ChangeDefaultProjectRequestDTO(
        UUID newDefaultProjectId
) {} 