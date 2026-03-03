package com.stodo.projectchaos.features.priority;

import com.stodo.projectchaos.features.priority.dto.response.TaskPriorityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectTaskPrioritiesController {
    private final TaskPriorityService taskPriorityService;

    @GetMapping("/{projectId}/task-priorities")
    public ResponseEntity<List<TaskPriorityResponseDTO>> getTaskPrioritiesByProjectId(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskPriorityService.findTaskPrioritiesByProjectId(projectId));
    }
}