package com.stodo.projectchaos.ai.features.chatmemory;

import com.stodo.projectchaos.model.entity.SpringAIChatMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringAIChatMemoryRepository extends JpaRepository<SpringAIChatMemoryEntity, UUID> {

    List<SpringAIChatMemoryEntity> findByConversationIdOrderByTimestampAsc(String conversationId);

    List<SpringAIChatMemoryEntity> findByConversation_IdAndConversation_Project_IdAndConversation_User_IdOrderByTimestampAsc(
            String conversationId, UUID projectId, UUID userId);

    void deleteByConversationId(String conversationId);

    void deleteByConversation_IdAndConversation_Project_IdAndConversation_User_Id(
            String conversationId, UUID projectId, UUID userId);
}