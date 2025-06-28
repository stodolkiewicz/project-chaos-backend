package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.project.create.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.model.dto.project.create.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.defaultproject.response.DefaultProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.byid.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    // not really used
    @GetMapping("/default")
    public DefaultProjectResponseDTO getDefaultProject(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        return projectService.getDefaultProjectForUser(email);
    }

}
