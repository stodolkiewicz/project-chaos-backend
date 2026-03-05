package com.stodo.projectchaos.features.invitation.dto.mapper;

import com.stodo.projectchaos.features.invitation.dto.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.features.invitation.dto.service.CreateInvitation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CreateInvitationMapper {
    CreateInvitationMapper INSTANCE = Mappers.getMapper(CreateInvitationMapper.class);

    CreateInvitationResponseDTO toCreateInvitationResponseDTO(CreateInvitation createInvitation);
}