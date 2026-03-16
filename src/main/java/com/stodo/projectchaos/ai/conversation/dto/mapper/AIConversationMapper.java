package com.stodo.projectchaos.ai.conversation.dto.mapper;

import com.stodo.projectchaos.ai.conversation.dto.response.ConversationResponseDTO;
import com.stodo.projectchaos.ai.conversation.dto.service.AIConversation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AIConversationMapper {
    AIConversationMapper INSTANCE = Mappers.getMapper(AIConversationMapper.class);

    ConversationResponseDTO toConversationResponseDTO(AIConversation conversation);

    List<ConversationResponseDTO> toConversationResponseDTOList(List<AIConversation> conversations);
}