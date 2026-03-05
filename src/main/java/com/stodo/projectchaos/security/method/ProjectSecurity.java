package com.stodo.projectchaos.security.method;

import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.model.entity.UserEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("projectSecurity")
public class ProjectSecurity {
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectSecurity(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    public boolean isAdminInProject(UUID projectId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();

        boolean userIsAdmin = projectService.isUserAdminInProject(email, projectId);
        if(!userIsAdmin) {
            throw new AccessDeniedException("Only project administrators can execute this action.");
        }
        return true;
    }

    public boolean hasAtLeastMemberRole(UUID projectId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();

        boolean hasAtLeastMemberRole = projectService.hasAtLeastMemberRole(email, projectId);
        if(!hasAtLeastMemberRole) {
            throw new AccessDeniedException("As a viewer, you can only view projects – editing and deleting is not allowed.");
        }
        return true;
    }

    public boolean affectedUserIsNotAdminInTheProject(UUID projectId, UUID affectedUserId) {
        return !projectService.isUserAdminInProject(affectedUserId, projectId);
    }

    public boolean isSelfModification(Authentication authentication, UUID userId) {
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found."));

        if (!userEntity.getId().equals(userId)) {
            throw new AccessDeniedException("You can only perform this action on your own account.");
        }
        return true;
    }
}
