package com.stodo.projectchaos.features.user.dto.response;

import java.util.UUID;

public record ChangeUserRoleResponseDTO(
        UUID projectId,
        UUID userId,
        String newRole
) {
}