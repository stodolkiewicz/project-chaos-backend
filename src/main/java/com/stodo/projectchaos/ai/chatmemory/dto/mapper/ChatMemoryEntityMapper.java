package com.stodo.projectchaos.ai.chatmemory.dto.mapper;

import com.stodo.projectchaos.ai.chatmemory.dto.service.ChatMemory;
import com.stodo.projectchaos.model.entity.SpringAIChatMemoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ChatMemoryEntityMapper {
    ChatMemoryEntityMapper INSTANCE = Mappers.getMapper(ChatMemoryEntityMapper.class);

    @Mapping(target = "type", expression = "java(mapMessageType(entity.getType()))")
    ChatMemory toChatMemory(SpringAIChatMemoryEntity entity);

    List<ChatMemory> toChatMemoryList(List<SpringAIChatMemoryEntity> entities);

    default ChatMemory.MessageType mapMessageType(SpringAIChatMemoryEntity.MessageType entityType) {
        return ChatMemory.MessageType.valueOf(entityType.name());
    }
}