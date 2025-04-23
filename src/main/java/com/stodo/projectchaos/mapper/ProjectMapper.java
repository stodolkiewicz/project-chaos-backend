package com.stodo.projectchaos.mapper;

import com.stodo.projectchaos.model.dto.response.DefaultProjectResponseDTO;
import com.stodo.projectchaos.model.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    DefaultProjectResponseDTO projectToDefaultProjectResponseDTO(ProjectEntity projectEntity);
}
