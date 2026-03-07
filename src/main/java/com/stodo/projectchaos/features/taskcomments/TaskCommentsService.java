package com.stodo.projectchaos.features.taskcomments;

import com.stodo.projectchaos.features.taskcomments.dto.mapper.TaskCommentsEntityMapper;
import com.stodo.projectchaos.features.taskcomments.dto.request.CreateTaskCommentRequestDTO;
import com.stodo.projectchaos.features.taskcomments.dto.request.UpdateTaskCommentRequestDTO;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskComment;
import com.stodo.projectchaos.model.entity.TaskComments;
import com.stodo.projectchaos.model.entity.TaskEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

@Service
public class TaskCommentsService {
    private final TaskCommentsRepository taskCommentsRepository;
    private final EntityManager entityManager;

    public TaskCommentsService(TaskCommentsRepository taskCommentsRepository, EntityManager entityManager) {
        this.taskCommentsRepository = taskCommentsRepository;
        this.entityManager = entityManager;
    }

    public List<TaskComment> getAllTaskComments(UUID taskId) {
        List<TaskComments> comments = taskCommentsRepository.findByTaskId(taskId);
        return comments.stream()
                .map(TaskCommentsEntityMapper.INSTANCE::toTaskComment)
                .toList();
    }

    public TaskComment createTaskComment(CreateTaskCommentRequestDTO request, UUID authorId) {
        TaskComments comment = new TaskComments();
        comment.setId(UUID.randomUUID());
        
        TaskEntity task = entityManager.getReference(TaskEntity.class, request.taskId());
        comment.setTask(task);
        
        UserEntity author = entityManager.getReference(UserEntity.class, authorId);
        comment.setAuthor(author);
        
        comment.setContent(request.content());
        
        if (request.replyToId() != null) {
            TaskComments replyTo = entityManager.getReference(TaskComments.class, request.replyToId());
            comment.setReplyTo(replyTo);
        }
        
        TaskComments savedComment = taskCommentsRepository.save(comment);
        return TaskCommentsEntityMapper.INSTANCE.toTaskComment(savedComment);
    }

    public TaskComment updateTaskComment(UUID commentId, UpdateTaskCommentRequestDTO request, UUID authorId) {
        TaskComments comment = taskCommentsRepository.findById(commentId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("commentId", commentId)
                        .entityType("TaskComments")
                        .build());
        
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("User can only edit own comments");
        }
        
        comment.setContent(request.content());
        TaskComments savedComment = taskCommentsRepository.save(comment);
        return TaskCommentsEntityMapper.INSTANCE.toTaskComment(savedComment);
    }

    public void deleteTaskComment(UUID commentId, UUID authorId) {
        TaskComments comment = taskCommentsRepository.findById(commentId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("commentId", commentId)
                        .entityType("TaskComments")
                        .build());
        
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("User can only delete own comments");
        }
        
        taskCommentsRepository.delete(comment);
    }
}
