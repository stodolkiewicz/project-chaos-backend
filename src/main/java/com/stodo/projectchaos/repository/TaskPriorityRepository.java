package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.TaskPriorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriorityEntity, UUID> {
    List<TaskPriorityEntity> findByProjectId(UUID projectId);
}
