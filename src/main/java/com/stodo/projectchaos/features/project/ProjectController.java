package com.stodo.projectchaos.features.project;

import com.stodo.projectchaos.features.project.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.features.project.dto.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.DeleteProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.UserProjectsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


}
