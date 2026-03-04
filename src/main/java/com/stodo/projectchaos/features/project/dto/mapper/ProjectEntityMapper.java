package com.stodo.projectchaos.features.project.dto.mapper;

import com.stodo.projectchaos.model.entity.ProjectEntity;
import com.stodo.projectchaos.features.project.dto.service.Project;
import com.stodo.projectchaos.features.project.dto.service.ProjectDelete;
import com.stodo.projectchaos.features.project.dto.service.UserProjects;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectEntityMapper {
    ProjectEntityMapper INSTANCE = Mappers.getMapper(ProjectEntityMapper.class);

    Project toProject(ProjectEntity entity);
    
    ProjectDelete toProjectDelete(ProjectEntity entity);
}