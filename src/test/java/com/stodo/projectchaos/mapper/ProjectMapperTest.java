package com.stodo.projectchaos.mapper;

import com.stodo.projectchaos.model.dto.response.DefaultProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {

    @Test
    void testProjectToDefaultProjectResponseDTO() {
        // Given
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(UUID.randomUUID());
        projectEntity.setName("Test Project");
        projectEntity.setDescription("This is a test project.");
        projectEntity.setCreatedDate(Instant.now());
        projectEntity.setLastModifiedDate(Instant.now());
        projectEntity.setLastModifiedBy("testUser");

        // When
        DefaultProjectResponseDTO responseDTO = ProjectMapper.INSTANCE.projectToDefaultProjectResponseDTO(projectEntity);

        // Then
        assertNotNull(responseDTO);
        assertEquals(projectEntity.getId(), responseDTO.id());
        assertEquals(projectEntity.getName(), responseDTO.name());
        assertEquals(projectEntity.getDescription(), responseDTO.description());
    }
}