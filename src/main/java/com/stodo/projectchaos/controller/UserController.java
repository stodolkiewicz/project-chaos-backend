package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.model.dto.user.update.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{projectId}/users")
    public ResponseEntity<ProjectUsersResponseDTO> getProjectById (@PathVariable UUID projectId) {
        return ResponseEntity.ok(userService.findProjectUsersByProjectId(projectId));
    }

    @PatchMapping("/default-project")
    public ResponseEntity<Void> changeDefaultProject(
            @RequestBody ChangeDefaultProjectRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.changeDefaultProject(request, email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<AssignUserToProjectResponseDTO> assignUserToProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody AssignUserToProjectRequestDTO assignUserRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();
        return ResponseEntity.ok(userService.assignUserToProject(projectId, assignUserRequest, adminEmail));
    }
}
