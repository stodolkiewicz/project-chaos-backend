package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.project.byid.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.create.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.model.dto.project.create.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.defaultproject.response.DefaultProjectIdResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.DeleteProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.model.dto.user.changerole.request.ChangeUserRoleRequestDTO;
import com.stodo.projectchaos.model.dto.user.changerole.response.ChangeUserRoleResponseDTO;
import com.stodo.projectchaos.model.dto.user.unassign.request.UnassignUserFromProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<CreateProjectResponseDTO> createProject(
            @RequestBody CreateProjectRequestDTO createProjectRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(projectService.createProject(createProjectRequestDTO, email));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById (@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.findProjectById(projectId));
    }

    // to be used to display list of projects for user
    @GetMapping
    public ResponseEntity<UserProjectsResponseDTO> getProjectList(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(projectService.findProjectsByUserEmail(email));
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<DeleteProjectResponseDTO> hardDeleteProject(@PathVariable("projectId") UUID projectId) {
        DeleteProjectResponseDTO deleteProjectResponseDTO = projectService.hardDeleteProject(projectId);

        return ResponseEntity.ok(deleteProjectResponseDTO);
    }

    @GetMapping("/{projectId}/users")
    public ResponseEntity<ProjectUsersResponseDTO> getProjectUsers (@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.findProjectUsersByProjectId(projectId));
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @PatchMapping("/{projectId}/users")
    public ResponseEntity<AssignUserToProjectResponseDTO> addUserToProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody AssignUserToProjectRequestDTO assignUserRequest) {
        return ResponseEntity.ok(projectService.assignUserToProject(projectId, assignUserRequest));
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @DeleteMapping("/{projectId}/users")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UnassignUserFromProjectRequestDTO unassignRequest) {
        projectService.removeUserFromProject(projectId, unassignRequest);
        return ResponseEntity.noContent().build();
    }

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
