package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.ColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, UUID> {
    List<ColumnEntity> findByProjectIdOrderByPosition(UUID projectId);
}
