package com.stodo.projectchaos.features.project;

import com.stodo.projectchaos.features.project.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.request.CreateProjectRequestDTO;
import com.stodo.projectchaos.features.project.dto.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.DeleteProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.features.project.dto.mapper.ProjectMapper;
import com.stodo.projectchaos.features.project.dto.service.Project;
import com.stodo.projectchaos.features.project.dto.service.ProjectDelete;
import com.stodo.projectchaos.features.project.dto.service.UserProjects;
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
        Project project = projectService.createProject(createProjectRequestDTO, email);
        CreateProjectResponseDTO responseDTO = ProjectMapper.INSTANCE.toCreateProjectResponseDTO(project);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById (@PathVariable UUID projectId) {
        Project project = projectService.findProjectById(projectId);
        ProjectResponseDTO responseDTO = ProjectMapper.INSTANCE.toProjectResponseDTO(project);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<UserProjectsResponseDTO> getProjectList(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UserProjects userProjects = projectService.findProjectsByUserEmail(email);
        UserProjectsResponseDTO responseDTO = ProjectMapper.INSTANCE.toUserProjectsResponseDTO(userProjects);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<DeleteProjectResponseDTO> hardDeleteProject(@PathVariable("projectId") UUID projectId) {
        ProjectDelete projectDelete = projectService.hardDeleteProject(projectId);
        DeleteProjectResponseDTO responseDTO = ProjectMapper.INSTANCE.toDeleteProjectResponseDTO(projectDelete);
        return ResponseEntity.ok(responseDTO);
    }


}
