package com.stodo.projectchaos.features.project.dto.mapper;


import com.stodo.projectchaos.features.project.dto.response.ProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.CreateProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.DeleteProjectResponseDTO;
import com.stodo.projectchaos.features.project.dto.response.UserProjectsResponseDTO;
import com.stodo.projectchaos.features.project.dto.service.Project;
import com.stodo.projectchaos.features.project.dto.service.ProjectDelete;
import com.stodo.projectchaos.features.project.dto.service.UserProjects;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    ProjectResponseDTO toProjectResponseDTO(Project project);
    
    CreateProjectResponseDTO toCreateProjectResponseDTO(Project project);
    
    DeleteProjectResponseDTO toDeleteProjectResponseDTO(ProjectDelete projectDelete);
    
    UserProjectsResponseDTO toUserProjectsResponseDTO(UserProjects userProjects);
}
