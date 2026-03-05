package com.stodo.projectchaos.features.projectuser;

import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.projectuser.dto.mapper.ProjectUserMapper;
import com.stodo.projectchaos.features.projectuser.dto.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.features.projectuser.dto.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.features.projectuser.dto.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.features.projectuser.dto.service.AssignUserToProject;
import com.stodo.projectchaos.features.projectuser.dto.service.ProjectUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectUsersController {

    private final ProjectUserService projectUserService;
    private final ProjectService projectService;

    @GetMapping("/{projectId}/users")
    public ResponseEntity<ProjectUsersResponseDTO> getProjectUsers (@PathVariable UUID projectId) {
        List<ProjectUser> projectUsers = projectUserService.findProjectUsersByProjectId(projectId);
        ProjectUsersResponseDTO response = ProjectUserMapper.INSTANCE.toProjectUsersResponseDTO(projectUsers);

        return ResponseEntity.ok(response);
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

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication) " +
            "AND @projectSecurity.userAboutToBeDeletedIsNotAdminInTheProject(#projectId, #userId)")
    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject (
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

}