package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.project.defaultproject.response.DefaultProjectIdResponseDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.model.dto.user.update.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.service.ProjectService;
import com.stodo.projectchaos.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProjectService projectService;

    @GetMapping("/{projectId}/users")
    public ResponseEntity<ProjectUsersResponseDTO> getProjectById (@PathVariable UUID projectId) {
        return ResponseEntity.ok(userService.findProjectUsersByProjectId(projectId));
    }

    @GetMapping("/default-project")
    public ResponseEntity<DefaultProjectIdResponseDTO> getDefaultProjectId(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<UUID> defaultProjectId = projectService.findDefaultProjectIdByEmail(email);

        return ResponseEntity.ok(new DefaultProjectIdResponseDTO(defaultProjectId.orElse(null)));
    }

    @PatchMapping("/default-project")
    public ResponseEntity<Void> changeDefaultProject(
            @RequestBody ChangeDefaultProjectRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.changeDefaultProject(request, email);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @PatchMapping("/projects/{projectId}")
    public ResponseEntity<AssignUserToProjectResponseDTO> assignUserToProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody AssignUserToProjectRequestDTO assignUserRequest) {
        return ResponseEntity.ok(userService.assignUserToProject(projectId, assignUserRequest));
    }
}
