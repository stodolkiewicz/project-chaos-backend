package com.stodo.projectchaos.ai.features.conversation.dto.mapper;

import com.stodo.projectchaos.ai.features.conversation.dto.service.AIConversation;
import com.stodo.projectchaos.model.entity.AIConversationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AIConversationEntityMapper {
    AIConversationEntityMapper INSTANCE = Mappers.getMapper(AIConversationEntityMapper.class);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "userId", source = "user.id")
    AIConversation toAIConversation(AIConversationEntity entity);

    List<AIConversation> toAIConversationList(List<AIConversationEntity> entities);
}