package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.model.dto.user.unassign.request.UnassignUserFromProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectUsersController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}/users")
    public ResponseEntity<ProjectUsersResponseDTO> getProjectUsers (@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.findProjectUsersByProjectId(projectId));
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @PatchMapping("/{projectId}/users")
    public ResponseEntity<AssignUserToProjectResponseDTO> addUserToProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody AssignUserToProjectRequestDTO assignUserRequest) {
        return ResponseEntity.ok(projectService.assignUserToProject(
                projectId,
                assignUserRequest.userEmail(),
                assignUserRequest.projectRole())
        );
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @DeleteMapping("/{projectId}/users")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UnassignUserFromProjectRequestDTO unassignRequest) {
        projectService.removeUserFromProject(projectId, unassignRequest);
        return ResponseEntity.noContent().build();
    }

}