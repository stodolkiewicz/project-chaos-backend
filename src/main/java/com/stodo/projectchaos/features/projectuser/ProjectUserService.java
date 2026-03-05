package com.stodo.projectchaos.features.projectuser;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.features.projectuser.dto.mapper.ProjectUserMapper;
import com.stodo.projectchaos.features.projectuser.dto.service.ProjectUser;
import com.stodo.projectchaos.features.user.dto.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import com.stodo.projectchaos.model.entity.ProjectUserId;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final UserRepository userRepository;
    private final ProjectUsersRepository projectUsersRepository;

    public List<ProjectUser> findProjectUsersByProjectId(UUID projectId) {
        List<ProjectUserQueryResponseDTO> projectUsersQueryResponse = userRepository.findProjectUsersByProjectId(projectId);
        
        return ProjectUserMapper.INSTANCE.toListOfProjectUsers(projectUsersQueryResponse);
    }

    public void leaveProject(UUID projectId, String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("email", userEmail)
                        .entityType("UserEntity")
                        .build());

        ProjectUsersEntity projectUser = projectUsersRepository
                .findById(new ProjectUserId(projectId, user.getId()))
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("ProjectUserId", new ProjectUserId(projectId, user.getId()))
                        .entityType("ProjectUsersEntity")
                        .build());

        if (projectUser.getProjectRole() == ProjectRoleEnum.ADMIN) {
            long adminCount = projectUsersRepository.countByProjectIdAndProjectRole(projectId, ProjectRoleEnum.ADMIN);
            if (adminCount <= 1) {
                throw new LastAdminCannotLeaveProjectException();
            }
        }

        projectUsersRepository.deleteById(new ProjectUserId(projectId, user.getId()));
        
        // If this was user's default project, we need to handle it
        if (user.getProject() != null && user.getProject().getId().equals(projectId)) {
            // Find another project this user has access to
            List<ProjectUsersEntity> userOtherProjects = projectUsersRepository
                    .findByUserIdAndProjectIdNot(user.getId(), projectId);
            
            if (!userOtherProjects.isEmpty()) {
                // Set the first available project as new default
                user.setProject(userOtherProjects.get(0).getProject());
            } else {
                // No other projects, clear default
                user.setProject(null);
            }
            userRepository.save(user);
        }
    }
}