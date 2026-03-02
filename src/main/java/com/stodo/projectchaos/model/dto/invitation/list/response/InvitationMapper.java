package com.stodo.projectchaos.model.dto.invitation.list.response;

import com.stodo.projectchaos.model.entity.InvitationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvitationMapper {
    InvitationMapper INSTANCE = Mappers.getMapper(InvitationMapper.class);

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "invitedBy.email", target = "invitedByEmail")
    InvitationResponseDTO toInvitationResponseDTO(InvitationEntity invitation);
}