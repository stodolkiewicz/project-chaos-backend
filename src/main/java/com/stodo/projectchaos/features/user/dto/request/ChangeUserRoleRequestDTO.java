package com.stodo.projectchaos.features.user.dto.request;

import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequestDTO(
        @NotNull(message = "Project role is required")
        ProjectRoleEnum projectRole
) {
}