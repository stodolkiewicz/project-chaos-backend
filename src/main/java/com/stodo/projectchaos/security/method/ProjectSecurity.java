package com.stodo.projectchaos.security.method;

import com.stodo.projectchaos.exception.UnauthorizedException;
import com.stodo.projectchaos.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
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
            throw new UnauthorizedException("The user is not the project ADMIN. Only project administrators can assign other users to the project.");
        }
        return projectService.isUserAdminInProject(email, projectId);
    }
}
