package com.stodo.projectchaos.mapper;

import com.stodo.projectchaos.model.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;

import java.util.UUID;

public class ProjectMapper {
    public static ProjectResponseDTO toDto(ProjectEntity projectEntity, UUID defaultProjectId) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(projectEntity.getId());
        dto.setName(projectEntity.getName());
        dto.setDescription(projectEntity.getDescription());
        dto.setDefaultProject(projectEntity.getId().equals(defaultProjectId));

        return dto;
    }
}