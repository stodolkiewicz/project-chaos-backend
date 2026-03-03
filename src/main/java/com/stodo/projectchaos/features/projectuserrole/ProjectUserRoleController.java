package com.stodo.projectchaos.features.projectuserrole;

import com.stodo.projectchaos.features.user.dto.request.ChangeUserRoleRequestDTO;
import com.stodo.projectchaos.features.user.dto.response.ChangeUserRoleResponseDTO;
import com.stodo.projectchaos.features.project.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectUserRoleController {

    private final ProjectService projectService;

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @PatchMapping("/{projectId}/users/{userId}/role")
    public ResponseEntity<ChangeUserRoleResponseDTO> changeUserRole (
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeUserRoleRequestDTO changeRoleRequest) {
        ChangeUserRoleResponseDTO response = projectService.changeUserRole(projectId, userId, changeRoleRequest);
        return ResponseEntity.ok(response);
    }
}