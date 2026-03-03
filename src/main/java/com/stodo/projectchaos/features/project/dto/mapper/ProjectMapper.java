package com.stodo.projectchaos.features.project.dto.mapper;


import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.features.project.dto.response.ProjectResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    ProjectResponseDTO toProjectResponseDTO(ProjectEntity project);
}
