package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.ProjectUserMapper;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.model.dto.user.update.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import com.stodo.projectchaos.model.entity.ProjectUserId;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import com.stodo.projectchaos.repository.ProjectUsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUsersRepository projectUsersRepository;

    public ProjectUsersResponseDTO findProjectUsersByProjectId(UUID projectId) {
        List<ProjectUserQueryResponseDTO> users = userRepository.findProjectUsersByProjectId(projectId);
        return ProjectUserMapper.INSTANCE.toProjectUsersResponseDTO(users);
    }

    public void changeDefaultProject(ChangeDefaultProjectRequestDTO request, String email) {
        CompletableFuture<UserEntity> userEntityCF = CompletableFuture.supplyAsync(
                () -> userRepository.findByEmail(email)
                        .orElseThrow(() -> EntityNotFoundException.builder()
                                .identifier("email", email)
                                .entityType("UserEntity")
                                .build()));

        CompletableFuture<ProjectEntity> projectEntityCF = CompletableFuture.supplyAsync(
                () -> projectRepository.findById(request.newDefaultProjectId())
                        .orElseThrow(() -> EntityNotFoundException.builder()
                                .identifier("projectId", request.newDefaultProjectId())
                                .entityType("ProjectEntity")
                                .build()));

        CompletableFuture<Void> cfs = CompletableFuture.allOf(userEntityCF, projectEntityCF);
        cfs.join();

        UserEntity userEntity = userEntityCF.join();
        ProjectEntity newDefaultProject = projectEntityCF.join();

        userEntity.setProject(newDefaultProject);
        userRepository.save(userEntity);
    }

    public AssignUserToProjectResponseDTO assignUserToProject(UUID projectId, 
                                                             AssignUserToProjectRequestDTO assignUserRequest) {
        String invitedEmail = assignUserRequest.userEmail();
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("projectId", projectId)
                        .entityType("ProjectEntity")
                        .build());

        UserEntity userToAssign = userRepository.findByEmail(invitedEmail)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", invitedEmail)
                        .entityType("UserEntity")
                        .build());

        // if user does not have defaultProject, assign it to him
        if(userToAssign.getProject() == null) {
            userToAssign.setProject(project);
        }

        Optional<ProjectUsersEntity> existing = projectUsersRepository
                .findById(new ProjectUserId(projectId, invitedEmail));

        ProjectUsersEntity projectUsersEntity = existing.orElseGet(() -> {
            ProjectUsersEntity newProjectUsersEntity = new ProjectUsersEntity();
            newProjectUsersEntity.setId(new ProjectUserId(projectId, invitedEmail));
            newProjectUsersEntity.setProject(project);
            newProjectUsersEntity.setUser(userToAssign);

            return newProjectUsersEntity;
        });

        // if projectUsers entity already exists, only role may have changed
        projectUsersEntity.setProjectRole(assignUserRequest.projectRole());
        projectUsersRepository.save(projectUsersEntity);

        String successMessage = "User " + invitedEmail + " successfully assigned to the project with role: " + assignUserRequest.projectRole().getRole() + ".";
        return new AssignUserToProjectResponseDTO(
                projectId,
                assignUserRequest.userEmail(),
                assignUserRequest.projectRole().getRole(),
                successMessage
        );
    }
}
