package com.stodo.projectchaos.features.projectuser.dto.mapper;

import com.stodo.projectchaos.features.user.dto.query.ProjectUserQueryResponseDTO;
import com.stodo.projectchaos.features.projectuser.dto.response.AssignUserToProjectResponseDTO;
import com.stodo.projectchaos.features.projectuser.dto.response.ProjectUsersResponseDTO;
import com.stodo.projectchaos.features.projectuser.dto.response.ProjectUserResponseDTO;
import com.stodo.projectchaos.features.projectuser.dto.service.AssignUserToProject;
import com.stodo.projectchaos.features.projectuser.dto.service.ProjectUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProjectUserMapper {
    ProjectUserMapper INSTANCE = Mappers.getMapper(ProjectUserMapper.class);

    // for assignUserToProject
    AssignUserToProjectResponseDTO toAssignUserToProjectResponseDTO(AssignUserToProject assignUserToProject);

    // for getProjectUsers
    List<ProjectUserResponseDTO> toProjectUserResponseDTOList(List<ProjectUser> projectUsers);
    default ProjectUsersResponseDTO toProjectUsersResponseDTO(List<ProjectUser> projectUsers) {
        List<ProjectUserResponseDTO> responseDtos = toProjectUserResponseDTOList(projectUsers);
        return new ProjectUsersResponseDTO(responseDtos);
    }
    ProjectUserResponseDTO toProjectUserResponseDTO(ProjectUser projectUser);
    
    List<ProjectUser> toListOfProjectUsers(List<ProjectUserQueryResponseDTO> projectUserQueryResponseDTOS);
}