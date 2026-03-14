package com.stodo.projectchaos.ai;

import com.stodo.projectchaos.model.entity.AIUsageLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AIUsageLogsRepository extends JpaRepository<AIUsageLogsEntity, UUID>{
}

