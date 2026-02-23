package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.project.list.response.DeleteProjectResponseDTO;
import com.stodo.projectchaos.security.method.ProjectSecurity;
import com.stodo.projectchaos.security.service.JwtService;
import com.stodo.projectchaos.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "spring.cloud.gcp.sql.enabled=false",
})
@EnableMethodSecurity // Method Security is turned off in @WebMvcTest by default
@WebMvcTest(ProjectController.class)
@Import(ProjectSecurity.class)
class ProjectControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    private static final UUID PROJECT_ID = UUID.randomUUID();

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ADMIN_EMAIL = "admin@test.com";

    private static final String MEMBER_ROLE = "MEMBER";
    private static final String MEMBER_EMAIL = "member@test.com";

    private static final String VIEWER_ROLE = "VIEWER";
    private static final String VIEWER_EMAIL = "viewer@test.com";

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = ADMIN_ROLE)
    void shouldReturn200WhenUserIsAdminInProject() throws Exception {
        when(projectService.isUserAdminInProject(eq(ADMIN_EMAIL), eq(PROJECT_ID))).thenReturn(true);
        when(projectService.hardDeleteProject(eq(PROJECT_ID)))
                .thenReturn(new DeleteProjectResponseDTO(PROJECT_ID));

        mockMvc.perform(delete("/api/v1/projects/{projectId}", PROJECT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(projectService).hardDeleteProject(eq(PROJECT_ID));
    }

    @Test
    void shouldReturn302RedirectWhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/projects/{projectId}", PROJECT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));

        // Verify service method was never called
        verify(projectService, never()).hardDeleteProject(any());
    }

    @Test
    @WithMockUser(username = VIEWER_EMAIL, roles = VIEWER_ROLE)
    void shouldReturn403WhenUserIsViewerInProject() throws Exception {
        when(projectService.isUserAdminInProject(eq(VIEWER_EMAIL), eq(PROJECT_ID))).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/{projectId}", PROJECT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Verify service method was never called
        verify(projectService, never()).hardDeleteProject(any());
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = MEMBER_ROLE)
    void shouldReturn403WhenUserIsMemberInProject() throws Exception {
        when(projectService.isUserAdminInProject(eq(MEMBER_EMAIL), eq(PROJECT_ID))).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/{projectId}", PROJECT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Verify service method was never called
        verify(projectService, never()).hardDeleteProject(any());
    }
}