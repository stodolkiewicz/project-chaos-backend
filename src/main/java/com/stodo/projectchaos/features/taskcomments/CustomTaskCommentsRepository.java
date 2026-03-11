package com.stodo.projectchaos.features.taskcomments;

import com.stodo.projectchaos.model.entity.TaskComments;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class CustomTaskCommentsRepository {
    @PersistenceContext
    private EntityManager em;

    public List<TaskComments> findCommentsWithReplies(List<UUID> rootCommentIds) {
        if (rootCommentIds == null || rootCommentIds.isEmpty()) {
            return new ArrayList<>();
        }

        return em.createQuery(
                "SELECT tc FROM TaskComments tc " +
                    "WHERE tc.id IN :ids OR tc.replyTo.id IN :ids " +
                    "ORDER BY tc.createdDate ASC", TaskComments.class)
                .setParameter("ids", rootCommentIds)
                .getResultList();
    }
}
