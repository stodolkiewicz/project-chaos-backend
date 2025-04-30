package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{projectId}/users")
    public ResponseEntity<ProjectUsersResponseDTO> getProjectById (@PathVariable UUID projectId) {
        return ResponseEntity.ok(userService.findProjectUsersByProjectId(projectId));
    }

}
