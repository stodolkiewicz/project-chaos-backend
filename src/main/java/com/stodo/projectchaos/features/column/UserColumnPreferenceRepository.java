package com.stodo.projectchaos.features.column;

import com.stodo.projectchaos.model.entity.UserColumnPreferenceEntity;
import com.stodo.projectchaos.model.entity.UserColumnPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserColumnPreferenceRepository extends JpaRepository<UserColumnPreferenceEntity, UserColumnPreferenceId> {
    List<UserColumnPreferenceEntity> findByUserIdAndColumnProjectId(UUID userId, UUID projectId);
}
