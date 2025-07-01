package com.stodo.projectchaos.model.dto.user.update.request;

import java.util.UUID;

public record ChangeDefaultProjectRequestDTO(
        UUID newDefaultProjectId
) {} 