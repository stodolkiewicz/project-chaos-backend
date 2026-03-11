package com.stodo.projectchaos.features.taskcomments;

import com.stodo.projectchaos.features.taskcomments.dto.mapper.TaskCommentsMapper;
import com.stodo.projectchaos.features.taskcomments.dto.request.CreateTaskCommentRequestDTO;
import com.stodo.projectchaos.features.taskcomments.dto.request.UpdateTaskCommentRequestDTO;
import com.stodo.projectchaos.features.taskcomments.dto.response.TaskCommentResponseDTO;
import com.stodo.projectchaos.features.taskcomments.dto.response.TaskCommentWithRepliesResponseDTO;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskComment;
import com.stodo.projectchaos.features.taskcomments.dto.service.TaskCommentWithReplies;
import com.stodo.projectchaos.features.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class TaskCommentsController {

    private final TaskCommentsService taskCommentsService;
    private final UserService userService;

    @GetMapping("/{projectId}/tasks/{taskId}/comments")
    public Page<TaskCommentWithRepliesResponseDTO> getAllTaskComments(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<TaskCommentWithReplies> allTaskComments = taskCommentsService.getAllTaskComments(projectId, taskId, pageable);
        return allTaskComments.map(TaskCommentsMapper.INSTANCE::toTaskCommentWithRepliesResponseDTO);
    }

    @PostMapping("/{projectId}/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCommentResponseDTO createTaskComment(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskCommentRequestDTO request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        UUID authorId = userService.getUserIdByEmail(userEmail);
        TaskComment comment = taskCommentsService.createTaskComment(request, authorId);
        return TaskCommentsMapper.INSTANCE.toTaskCommentResponseDTO(comment);
    }

    @PutMapping("/{projectId}/tasks/{taskId}/comments/{commentId}")
    public TaskCommentResponseDTO updateTaskComment(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateTaskCommentRequestDTO request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        UUID authorId = userService.getUserIdByEmail(userEmail);
        TaskComment comment = taskCommentsService.updateTaskComment(commentId, request, authorId);
        return TaskCommentsMapper.INSTANCE.toTaskCommentResponseDTO(comment);
    }

    @DeleteMapping("/{projectId}/tasks/{taskId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskComment(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        UUID authorId = userService.getUserIdByEmail(userEmail);
        taskCommentsService.deleteTaskComment(commentId, authorId);
    }
}
