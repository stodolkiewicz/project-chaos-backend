package com.stodo.projectchaos.features.user.dto.mapper;

import com.stodo.projectchaos.features.user.dto.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.features.user.dto.response.ProjectUsersResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProjectUserMapper {
    ProjectUserMapper INSTANCE = Mappers.getMapper(ProjectUserMapper.class);

    default ProjectUsersResponseDTO toProjectUsersResponseDTO(List<ProjectUserQueryResponseDTO> users) {
        return new ProjectUsersResponseDTO(users);
    }
} 