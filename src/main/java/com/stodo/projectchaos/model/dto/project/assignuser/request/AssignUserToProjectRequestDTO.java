package com.stodo.projectchaos.model.dto.project.assignuser.request;

import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AssignUserToProjectRequestDTO(
    @NotNull(message = "User email is required")
    @Email(message = "Invalid email format")
    String userEmail,
    @NotNull(message = "Project role is required")
    ProjectRoleEnum projectRole
) {} 