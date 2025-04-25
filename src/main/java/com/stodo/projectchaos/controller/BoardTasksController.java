package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.response.boardtasks.BoardTasksResponseDTO;
import com.stodo.projectchaos.service.TaskService;
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
}
