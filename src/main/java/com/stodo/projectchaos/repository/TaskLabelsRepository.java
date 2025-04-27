package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.TaskLabelsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskLabelsRepository extends JpaRepository<TaskLabelsEntity, UUID> {
}
