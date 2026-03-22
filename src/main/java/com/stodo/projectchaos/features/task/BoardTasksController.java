package com.stodo.projectchaos.features.task;

import com.stodo.projectchaos.features.task.dto.request.UpdateTaskColumnRequestDTO;
import com.stodo.projectchaos.features.task.dto.response.TasksResponseDTO;
import com.stodo.projectchaos.features.task.dto.response.UpdateTaskColumnResponseDTO;
import com.stodo.projectchaos.features.task.dto.service.TaskColumnUpdate;
import com.stodo.projectchaos.features.task.dto.mapper.UpdateTaskColumnMapper;
import com.stodo.projectchaos.features.task.dto.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.features.task.dto.response.CreateTaskResponseDTO;
import com.stodo.projectchaos.features.task.dto.mapper.TaskMapper;
import com.stodo.projectchaos.features.task.dto.request.MoveTasksRequestDTO;
import com.stodo.projectchaos.model.enums.TaskStageEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class BoardTasksController {
    private final TaskService taskService;

    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<TasksResponseDTO>> getTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStageEnum stage) {
        List<TasksResponseDTO> response = taskService.getTasks(projectId, stage)
                .stream()
                .map(TaskMapper.INSTANCE::toBoardTasksResponseDTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<CreateTaskResponseDTO> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequestDTO requestDTO
    ) {
        CreateTaskResponseDTO responseDTO = TaskMapper.INSTANCE.toCreateTaskResponseDTO(
                taskService.createTask(requestDTO, projectId)
        );
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @DeleteMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId, @PathVariable UUID projectId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PatchMapping("/{projectId}/tasks/{taskId}")
        public ResponseEntity<UpdateTaskColumnResponseDTO> updateTaskColumn (
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskColumnRequestDTO requestDTO)
    {
            TaskColumnUpdate taskColumnUpdate = taskService.updateTaskColumn(requestDTO, taskId);
            UpdateTaskColumnResponseDTO responseDTO = UpdateTaskColumnMapper.INSTANCE.toUpdateTaskColumnResponseDTO(taskColumnUpdate);
            return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PostMapping("/{projectId}/tasks/move-to-backlog")
    public ResponseEntity<Void> moveTasksToBacklog(
            @PathVariable UUID projectId,
            @Valid @RequestBody MoveTasksRequestDTO requestDTO) {
        taskService.moveTasksToBacklog(requestDTO.taskIds(), projectId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PostMapping("/{projectId}/tasks/move-to-archive")
    public ResponseEntity<Void> moveTasksToArchive(
            @PathVariable UUID projectId,
            @Valid @RequestBody MoveTasksRequestDTO requestDTO) {
        taskService.moveTasksToArchive(requestDTO.taskIds(), projectId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@projectSecurity.hasAtLeastMemberRole(#projectId, authentication)")
    @PostMapping("/{projectId}/tasks/move-to-board")
    public ResponseEntity<Void> moveTasksToBoard(
            @PathVariable UUID projectId,
            @Valid @RequestBody MoveTasksRequestDTO requestDTO) {
        taskService.moveTasksToBoard(requestDTO.taskIds(), projectId);
        return ResponseEntity.noContent().build();
    }
}
