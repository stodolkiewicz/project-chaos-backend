package com.stodo.projectchaos.features.projectuser;

import com.stodo.projectchaos.features.user.dto.mapper.ProjectUserMapper;
import com.stodo.projectchaos.features.user.dto.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.features.user.dto.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.features.user.dto.request.UnassignUserFromProjectRequestDTO;
import com.stodo.projectchaos.features.user.dto.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.user.dto.service.AssignUserToProject;
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
    public ResponseEntity<AssignUserToProjectResponseDTO> assignUserToProject (
            @PathVariable UUID projectId,
            @Valid @RequestBody AssignUserToProjectRequestDTO assignUserRequest) {

        AssignUserToProject assignUserToProject = projectService.assignUserToProjectAndHandleUserDefaultProject(
                projectId,
                assignUserRequest.userEmail(),
                assignUserRequest.projectRole());
        AssignUserToProjectResponseDTO assignUserToProjectResponseDTO = ProjectUserMapper.INSTANCE.toAssignUserToProjectResponseDTO(assignUserToProject);

        return ResponseEntity.ok(assignUserToProjectResponseDTO);
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