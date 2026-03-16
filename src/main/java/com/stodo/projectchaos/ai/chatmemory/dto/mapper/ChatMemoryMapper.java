package com.stodo.projectchaos.ai.chatmemory.dto.mapper;

import com.stodo.projectchaos.ai.chatmemory.dto.response.ChatMemoryResponseDTO;
import com.stodo.projectchaos.ai.chatmemory.dto.service.ChatMemory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ChatMemoryMapper {
    ChatMemoryMapper INSTANCE = Mappers.getMapper(ChatMemoryMapper.class);

    @Mapping(target = "type", expression = "java(mapMessageType(chatMemory.type()))")
    ChatMemoryResponseDTO toChatMemoryResponseDTO(ChatMemory chatMemory);

    List<ChatMemoryResponseDTO> toChatMemoryResponseDTOList(List<ChatMemory> chatMemories);

    default ChatMemoryResponseDTO.MessageType mapMessageType(ChatMemory.MessageType serviceType) {
        return ChatMemoryResponseDTO.MessageType.valueOf(serviceType.name());
    }
}