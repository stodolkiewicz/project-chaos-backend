package com.stodo.projectchaos.features.invitation.dto.mapper;

import com.stodo.projectchaos.features.invitation.dto.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.model.entity.InvitationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreateInvitationMapper {
    CreateInvitationMapper INSTANCE = Mappers.getMapper(CreateInvitationMapper.class);

    @Mapping(source = "project.id", target = "projectId")
    CreateInvitationResponseDTO toCreateInvitationResponseDTO(InvitationEntity invitation);
}