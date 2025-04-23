package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.response.DefaultProjectResponseDTO;
import com.stodo.projectchaos.model.dto.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<UserProjectsResponseDTO> getProjectsByUserEmail(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(projectService.findProjectsByUserEmail(email));
    }

    @GetMapping("/default")
    public DefaultProjectResponseDTO getDefaultProject(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        return projectService.getDefaultProjectForUser(email);
    }

}
