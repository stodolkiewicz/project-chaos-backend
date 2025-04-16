package com.stodo.projectchaos.service;

import static org.junit.jupiter.api.Assertions.*;
import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.repository.CustomProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;

class ProjectServiceTest {
    private CustomProjectRepository customProjectRepository;
    private UserRepository userRepository;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        customProjectRepository = mock(CustomProjectRepository.class);
        userRepository = mock(UserRepository.class);
        projectService = new ProjectService(customProjectRepository, userRepository);
    }

    @Test
    void shouldReturnProjects_whenUserExists() {
        // given
        String email = "test@example.com";
        UUID projectId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        ProjectEntity defaultProject = new ProjectEntity();
        defaultProject.setId(projectId);
        user.setProject(defaultProject);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(customProjectRepository.findProjectsByUserEmail(email)).thenReturn(List.of(projectEntity));

        // when
        List<ProjectResponseDTO> result = projectService.findProjectsByUserEmail(email);

        // then
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        // given
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when / then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                projectService.findProjectsByUserEmail(email));

        assertEquals("User", exception.getEntityType());
        assertTrue(exception.getIdentifiers().containsKey("email"));
        assertEquals(email, exception.getIdentifiers().get("email"));
    }
}