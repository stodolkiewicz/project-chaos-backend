package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.LabelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<LabelEntity, UUID> {
    Optional<LabelEntity> findByNameAndProjectId(String name, UUID projectId);
    @Query("SELECT  l " +
            "FROM LabelEntity l " +
            "WHERE l.project.id = :projectId")
    List<LabelEntity> findProjectLabelsByProjectId(UUID projectId);
}
