package com.stodo.projectchaos.features.taskcomments;

import com.stodo.projectchaos.model.entity.TaskCommentReadEntity;
import com.stodo.projectchaos.model.entity.TaskCommentReadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCommentReadRepository extends JpaRepository<TaskCommentReadEntity, TaskCommentReadId> {
}
