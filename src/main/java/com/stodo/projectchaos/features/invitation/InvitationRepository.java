package com.stodo.projectchaos.features.invitation;

import com.stodo.projectchaos.model.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, UUID> {
    
    List<InvitationEntity> findByEmail(String email);
    
    List<InvitationEntity> findByProjectId(UUID projectId);

    Optional<InvitationEntity> findByIdAndProjectId(UUID id, UUID projectId);

    boolean existsByEmailAndProjectId(String email, UUID projectId);
}