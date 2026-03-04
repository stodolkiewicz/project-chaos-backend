package com.stodo.projectchaos.features.invitation.dto.mapper;

import com.stodo.projectchaos.model.entity.InvitationEntity;
import com.stodo.projectchaos.features.invitation.dto.service.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvitationEntityMapper {
    InvitationEntityMapper INSTANCE = Mappers.getMapper(InvitationEntityMapper.class);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "invitedById", source = "invitedBy.id")
    @Mapping(target = "invitedByEmail", source = "invitedBy.email")
    Invitation toInvitation(InvitationEntity entity);
}