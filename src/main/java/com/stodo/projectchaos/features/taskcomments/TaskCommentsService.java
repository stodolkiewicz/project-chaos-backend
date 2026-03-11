package com.stodo.projectchaos.features.taskcomments;

import com.stodo.projectchaos.features.task.TaskRepository;
import com.stodo.projectchaos.features.taskcomments.dto.mapper.TaskCommentsEntityMapper;
import com.stodo.projectchaos.features.taskcomments.dto.mapper.TaskCommentsMapper;
import com.stodo.projectchaos.features.taskcomments.dto.request.CreateTaskCommentRequestDTO;
import com.stodo.projectchaos.features.taskcomments.dto.request.UpdateTaskCommentRequestDTO;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskComment;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskCommentWithReplies;
import com.stodo.projectchaos.model.entity.TaskComments;
import com.stodo.projectchaos.model.entity.TaskEntity;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class TaskCommentsService {
    private final TaskCommentsRepository taskCommentsRepository;
    private final CustomTaskCommentsRepository customTaskCommentsRepository;
    private final TaskRepository taskRepository;
    private final EntityManager entityManager;

    public TaskCommentsService(TaskCommentsRepository taskCommentsRepository, CustomTaskCommentsRepository customTaskCommentsRepository, TaskRepository taskRepository, EntityManager entityManager) {
        this.taskCommentsRepository = taskCommentsRepository;
        this.customTaskCommentsRepository = customTaskCommentsRepository;
        this.taskRepository = taskRepository;
        this.entityManager = entityManager;
    }

    public Page<TaskCommentWithReplies> getAllTaskComments(UUID projectId, UUID taskId, Pageable pageable) {
        if(taskRepository.notExistByIdAndProjectId(taskId, projectId)) {
            throw EntityNotFoundException.builder()
                    .entityType("Task")
                    .identifier("taskId", taskId)
                    .identifier("projectId", projectId)
                    .build();
        }

        Page<UUID> rootCommentIdsPage = taskCommentsRepository.findRootCommentIds(taskId, pageable);
        if(rootCommentIdsPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<UUID> rootCommentIds = rootCommentIdsPage.getContent();

        List<TaskComments> commentsAndRepliesEntities = customTaskCommentsRepository.findCommentsWithReplies(rootCommentIds);
        
        // Map to domain objects
        List<TaskComment> commentsAndReplies = commentsAndRepliesEntities.stream()
                .map(TaskCommentsEntityMapper.INSTANCE::toTaskComment)
                .toList();
        
        // Build hierarchical structure
        List<TaskCommentWithReplies> result = buildCommentStructure(commentsAndReplies, rootCommentIds);
        
        return new PageImpl<>(result, pageable, rootCommentIdsPage.getTotalElements());
    }

    private List<TaskCommentWithReplies> buildCommentStructure(List<TaskComment> allComments, List<UUID> rootCommentIds) {
        List<TaskCommentWithReplies> allCommentWithReplies = TaskCommentsMapper.INSTANCE.toTaskCommentWithReplies(allComments);
        
        // create map for quick access
        Map<UUID, TaskCommentWithReplies> allCommentsWithRepliesMap = allCommentWithReplies.stream()
                .collect(Collectors.toMap(TaskCommentWithReplies::getId, dto -> dto));
        
        // add replies to parents
        for (TaskComment comment : allComments) {
            if (comment.replyToId() != null) {
                TaskCommentWithReplies parent = allCommentsWithRepliesMap.get(comment.replyToId());
                TaskCommentWithReplies child = allCommentsWithRepliesMap.get(comment.id());
                if (parent != null && child != null) {
                    parent.getReplies().add(child);
                }
            }
        }
        
        // explicitly sort replies by createdDate for each root comment
        List<TaskCommentWithReplies> result = rootCommentIds.stream()
                .map(allCommentsWithRepliesMap::get)
                .toList();
        
        // ensure replies are sorted chronologically
        result.forEach(rootComment -> 
            rootComment.getReplies().sort(Comparator.comparing(TaskCommentWithReplies::getCreatedDate))
        );
        
        return result;
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
