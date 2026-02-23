package com.stodo.projectchaos.unit.service;

import com.stodo.projectchaos.service.ProjectService;
import com.stodo.projectchaos.model.dto.project.list.query.UserProjectQueryResponseDTO;
import com.stodo.projectchaos.model.dto.project.list.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.repository.CustomProjectRepository;
import com.stodo.projectchaos.repository.ProjectRepository;
import com.stodo.projectchaos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private CustomProjectRepository customProjectRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;


    @Test
    void shouldReturnUserProjects() {
        // Given
        String email = "user@example.com";
        UUID projectId = UUID.randomUUID();
        UserProjectQueryResponseDTO project = new UserProjectQueryResponseDTO(
                projectId,
                "Project Chaos",
                "Test project description",
                Instant.now(),
                ProjectRoleEnum.ADMIN,
                Instant.now()
        );

        List<UserProjectQueryResponseDTO> projects = List.of(project);
        Optional<UUID> defaultProjectId = Optional.of(projectId);

        when(customProjectRepository.findProjectsByUserEmail(email)).thenReturn(projects);
        when(userRepository.findDefaultProjectIdByEmail(email)).thenReturn(defaultProjectId);

        // when
        UserProjectsResponseDTO responseDTO = projectService.findProjectsByUserEmail(email);

        // then
        assertNotNull(responseDTO);
        assertEquals(1, responseDTO.projects().size());
        assertEquals(projectId, responseDTO.defaultProjectId());
        assertEquals("Project Chaos", responseDTO.projects().get(0).projectName());
        assertEquals("Test project description", responseDTO.projects().get(0).projectDescription());
    }

    @Test
    void shouldReturnEmptyProjectsWhenNoProjectsFound() {
        // given
        String email = "user@example.com";
        Optional<UUID> defaultProjectId = Optional.empty();

        when(customProjectRepository.findProjectsByUserEmail(email)).thenReturn(List.of());
        when(userRepository.findDefaultProjectIdByEmail(email)).thenReturn(defaultProjectId);

        // when
        UserProjectsResponseDTO responseDTO = projectService.findProjectsByUserEmail(email);

        // then
        assertNotNull(responseDTO);
        assertTrue(responseDTO.projects().isEmpty());
        assertNull(responseDTO.defaultProjectId());
    }

    @Test
    void shouldHandleDefaultProjectNotPresent() {
        // given
        String email = "user@example.com";
        UUID projectId = UUID.randomUUID();
        UserProjectQueryResponseDTO project = new UserProjectQueryResponseDTO(
                projectId,
                "Project Chaos",
                "Test project description",
                Instant.now(),
                ProjectRoleEnum.ADMIN,
                Instant.now()
        );

        List<UserProjectQueryResponseDTO> projects = List.of(project);

        when(customProjectRepository.findProjectsByUserEmail(email)).thenReturn(projects);
        when(userRepository.findDefaultProjectIdByEmail(email)).thenReturn(Optional.empty());

        // when
        UserProjectsResponseDTO responseDTO = projectService.findProjectsByUserEmail(email);

        // then
        assertNotNull(responseDTO);
        assertEquals(1, responseDTO.projects().size());
        assertNull(responseDTO.defaultProjectId());  // defaultProjectId is empty
    }

}