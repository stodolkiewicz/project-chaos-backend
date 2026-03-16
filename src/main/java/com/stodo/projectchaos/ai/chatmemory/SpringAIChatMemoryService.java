package com.stodo.projectchaos.ai.chatmemory;

import com.stodo.projectchaos.ai.chatmemory.dto.mapper.ChatMemoryEntityMapper;
import com.stodo.projectchaos.ai.chatmemory.dto.service.ChatMemory;
import com.stodo.projectchaos.model.entity.SpringAIChatMemoryEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpringAIChatMemoryService {

    private final SpringAIChatMemoryRepository repository;

    public List<ChatMemory> getChatHistory(String conversationId) {
        List<SpringAIChatMemoryEntity> entities = repository.findByConversationIdOrderByTimestampAsc(conversationId);
        return ChatMemoryEntityMapper.INSTANCE.toChatMemoryList(entities);
    }

    @Transactional
    public void deleteChatHistory(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

}