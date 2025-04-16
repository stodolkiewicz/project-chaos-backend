package com.stodo.projectchaos.model.mapper;

import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {

    private final ProjectMapper mapper = ProjectMapper.MAPPER;

    @Test
    void shouldMapProjectEntityToDto() {
        // given
        UUID id = UUID.randomUUID();
        ProjectEntity entity = ProjectEntity.builder()
                .id(id)
                .name("Test Project")
                .description("Test Description")
                .build();

        // when
        ProjectResponseDTO dto = mapper.toDto(entity);

        // then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("Test Project", dto.getName());
        assertEquals("Test Description", dto.getDescription());
    }
}