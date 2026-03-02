package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, UUID> {
    
    List<InvitationEntity> findByEmail(String email);
    
    List<InvitationEntity> findByProjectId(UUID projectId);
    
    boolean existsByEmailAndProjectId(String email, UUID projectId);
}