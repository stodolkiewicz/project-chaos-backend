package com.stodo.projectchaos.repository;

import com.stodo.projectchaos.model.entity.ProjectUserId;
import com.stodo.projectchaos.model.entity.ProjectUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProjectUsersRepository extends JpaRepository<ProjectUsersEntity, ProjectUserId> {
}
