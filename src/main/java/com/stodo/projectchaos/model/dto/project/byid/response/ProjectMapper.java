package com.stodo.projectchaos.model.dto.project.byid.response;


import com.stodo.projectchaos.model.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    ProjectResponseDTO toProjectResponseDTO(ProjectEntity project);
}
