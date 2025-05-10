package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.task.columnupdate.request.UpdateTaskColumnRequestDTO;
import com.stodo.projectchaos.model.dto.task.columnupdate.response.UpdateTaskColumnResponseDTO;
import com.stodo.projectchaos.model.dto.task.create.request.CreateTaskRequestDTO;
import com.stodo.projectchaos.model.dto.task.board.response.BoardTasksResponseDTO;
import com.stodo.projectchaos.model.dto.task.create.response.CreateTaskResponseDTO;
import com.stodo.projectchaos.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class BoardTasksController {
    private final TaskService taskService;

    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<BoardTasksResponseDTO>> getBoardTasks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getBoardTasks(projectId));
    }

    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<CreateTaskResponseDTO> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequestDTO requestDTO
    ) {
        CreateTaskResponseDTO responseDTO = taskService.createTask(requestDTO, projectId);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId, @PathVariable UUID projectId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{projectId}/tasks/{taskId}")
        public ResponseEntity<UpdateTaskColumnResponseDTO> updateTaskColumn (
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskColumnRequestDTO requestDTO)
    {
            UpdateTaskColumnResponseDTO updateTaskColumnResponseDTO = taskService.updateTaskColumn(requestDTO, taskId);
            return ResponseEntity.ok(updateTaskColumnResponseDTO);
        }
}
