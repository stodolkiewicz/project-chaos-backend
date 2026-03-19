package com.stodo.projectchaos.ai.features.chatmemory;

import com.stodo.projectchaos.ai.features.chatmemory.dto.mapper.ChatMemoryEntityMapper;
import com.stodo.projectchaos.ai.features.chatmemory.dto.service.ChatMemory;
import com.stodo.projectchaos.model.entity.SpringAIChatMemoryEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpringAIChatMemoryService {

    private final SpringAIChatMemoryRepository repository;

    public List<ChatMemory> getChatHistory(String conversationId) {
        List<SpringAIChatMemoryEntity> entities = repository.findByConversationIdOrderByTimestampAsc(conversationId);
        return ChatMemoryEntityMapper.INSTANCE.toChatMemoryList(entities);
    }

    public List<ChatMemory> getChatHistory(String conversationId, UUID projectId, UUID userId) {
        List<SpringAIChatMemoryEntity> entities = repository.findByConversation_IdAndConversation_Project_IdAndConversation_User_IdOrderByTimestampAsc(
                conversationId, projectId, userId);
        return ChatMemoryEntityMapper.INSTANCE.toChatMemoryList(entities);
    }

    @Transactional
    public void deleteChatHistory(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

    @Transactional
    public void deleteChatHistory(String conversationId, UUID projectId, UUID userId) {
        repository.deleteByConversation_IdAndConversation_Project_IdAndConversation_User_Id(
                conversationId, projectId, userId);
    }

}