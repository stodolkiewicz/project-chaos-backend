package com.stodo.projectchaos.service;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.ProjectUserMapper;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.model.dto.user.update.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.request.AssignUserToProjectRequestDTO;
import com.stodo.projectchaos.model.dto.user.assignuser.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.model.dto.user.changerole.request.ChangeUserRoleRequestDTO;
import com.stodo.projectchaos.model.dto.user.changerole.response.ChangeUserRoleResponseDTO;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import com.stodo.projectchaos.model.entity.ProjectUserId;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import com.stodo.projectchaos.repository.ProjectUsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    public void removeUserFromProject(UUID projectId, String userEmail) {
        // Check if user exists in project
        ProjectUsersEntity projectUser = projectUsersRepository
                .findById(new ProjectUserId(projectId, userEmail))
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("userEmail", userEmail)
                        .entityType("ProjectUsersEntity")
                        .build());

        // Check if user is admin and if removing would leave project without admins
        if (projectUser.getProjectRole() == ProjectRoleEnum.ADMIN) {
            long adminCount = projectUsersRepository.countByProjectIdAndProjectRole(projectId, ProjectRoleEnum.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove last admin from project. Please assign another admin first.");
            }
        }

        // Remove user from project
        projectUsersRepository.delete(projectUser);

        // If this was user's default project, clear it
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", userEmail)
                        .entityType("UserEntity")
                        .build());

        if (user.getProject() != null && user.getProject().getId().equals(projectId)) {
            user.setProject(null);
            userRepository.save(user);
        }
    }

    public ChangeUserRoleResponseDTO changeUserRole(UUID projectId, String userEmail, ChangeUserRoleRequestDTO changeRoleRequest) {
        // Check if user exists in project
        ProjectUsersEntity projectUser = projectUsersRepository
                .findById(new ProjectUserId(projectId, userEmail))
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("userEmail", userEmail)
                        .entityType("ProjectUsersEntity")
                        .build());

        ProjectRoleEnum currentRole = projectUser.getProjectRole();
        ProjectRoleEnum newRole = changeRoleRequest.projectRole();

        // Check if demoting last admin
        if (currentRole == ProjectRoleEnum.ADMIN && newRole != ProjectRoleEnum.ADMIN) {
            long adminCount = projectUsersRepository.countByProjectIdAndProjectRole(projectId, ProjectRoleEnum.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot demote last admin. Please assign another admin first.");
            }
        }

        // Update user role
        projectUser.setProjectRole(newRole);
        projectUsersRepository.save(projectUser);

        return new ChangeUserRoleResponseDTO(
                projectId,
                userEmail,
                newRole.getRole()
        );
    }
}
