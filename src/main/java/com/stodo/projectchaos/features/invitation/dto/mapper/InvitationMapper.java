package com.stodo.projectchaos.features.invitation.dto.mapper;

import com.stodo.projectchaos.features.invitation.dto.response.CreateInvitationResponseDTO;
import com.stodo.projectchaos.features.invitation.dto.response.InvitationResponseDTO;
import com.stodo.projectchaos.features.invitation.dto.service.CreateInvitation;
import com.stodo.projectchaos.features.invitation.dto.service.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvitationMapper {
    InvitationMapper INSTANCE = Mappers.getMapper(InvitationMapper.class);

    // service -> controller
    CreateInvitationResponseDTO toCreateInvitationResponseDTO(CreateInvitation createInvitation);
    
    InvitationResponseDTO toInvitationResponseDTO(Invitation invitation);
}