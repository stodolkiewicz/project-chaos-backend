package com.stodo.projectchaos.ai.features.conversation;

import com.stodo.projectchaos.model.entity.AIConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversationEntity, String> {

    List<AIConversationEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<AIConversationEntity> findByProjectIdAndUserIdOrderByCreatedAtDesc(UUID projectId, UUID userId);

    @Query("SELECT c FROM AIConversationEntity c WHERE c.project.id = :projectId ORDER BY c.createdAt DESC")
    List<AIConversationEntity> findConversationsByProjectId(@Param("projectId") UUID projectId);

    boolean existsByIdAndProjectId(String id, UUID projectId);
    
    void deleteByProjectId(UUID projectId);
}