package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.ProjectBacklogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectBacklogRepository extends JpaRepository<ProjectBacklogEntity, UUID> {
}
