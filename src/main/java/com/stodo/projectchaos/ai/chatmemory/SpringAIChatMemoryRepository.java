package com.stodo.projectchaos.ai.chatmemory;

import com.stodo.projectchaos.model.entity.SpringAIChatMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringAIChatMemoryRepository extends JpaRepository<SpringAIChatMemoryEntity, UUID> {

    List<SpringAIChatMemoryEntity> findByConversationIdOrderByTimestampAsc(String conversationId);

    void deleteByConversationId(String conversationId);
}