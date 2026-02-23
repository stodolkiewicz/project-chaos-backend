package com.stodo.projectchaos.security.method;

import com.stodo.projectchaos.service.ProjectService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("projectSecurity")
public class ProjectSecurity {
    private final ProjectService projectService;

    public ProjectSecurity(ProjectService projectService) {
        this.projectService = projectService;
    }

    public boolean isAdminInProject(UUID projectId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();

        boolean userIsAdmin = projectService.isUserAdminInProject(email, projectId);
        if(!userIsAdmin) {
            throw new AccessDeniedException("Only project administrators can assign other users to the project.");
        }
        return true;
    }

    public boolean hasAtLeastMemberRole(UUID projectId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();

        boolean hasAtLeastMemberRole = projectService.hasAtLeastMemberRole(email, projectId);
        if(!hasAtLeastMemberRole) {
            throw new AccessDeniedException("As a viewer, you can only view projects – editing is not allowed.");
        }
        return true;
    }
}
