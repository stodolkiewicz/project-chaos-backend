package com.stodo.projectchaos.model.dto.user.projectusers;

import com.stodo.projectchaos.model.dto.user.projectusers.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.model.dto.user.projectusers.response.ProjectUsersResponseDTO;
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