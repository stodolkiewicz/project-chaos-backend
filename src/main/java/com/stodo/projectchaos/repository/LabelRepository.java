package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.LabelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<LabelEntity, UUID> {
    Optional<LabelEntity> findByName(String name);
}
