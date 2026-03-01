package com.stodo.projectchaos.model.dto.user.changerole.response;

import java.util.UUID;

public record ChangeUserRoleResponseDTO(
        UUID projectId,
        UUID userId,
        String newRole
) {
}