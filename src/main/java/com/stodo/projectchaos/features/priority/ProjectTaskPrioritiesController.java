package com.stodo.projectchaos.features.priority;

import com.stodo.projectchaos.features.priority.dto.request.UpdateTaskPrioritiesRequestDTO;
import com.stodo.projectchaos.features.priority.dto.response.TaskPriorityResponseDTO;
import com.stodo.projectchaos.features.priority.dto.mapper.TaskPriorityMapper;
import com.stodo.projectchaos.security.filter.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        List<TaskPriorityResponseDTO> response = TaskPriorityMapper.INSTANCE.toTaskPriorityResponseDTOList(
                taskPriorityService.findTaskPrioritiesByProjectId(projectId)
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@projectSecurity.isAdminInProject(#projectId, authentication)")
    @PutMapping("/{projectId}/task-priorities")
    public ResponseEntity<Void> updateTaskPriorities(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateTaskPrioritiesRequestDTO requestDTO,
            Authentication authentication) {
        UUID userId = ((AppUserDetails) authentication.getPrincipal()).getUserId();
        taskPriorityService.updateTaskPriorities(
                projectId,
                userId,
                requestDTO.priorities()
                        .stream()
                        .map(TaskPriorityMapper.INSTANCE::toTaskPriority)
                        .toList()
        );
        return ResponseEntity.noContent().build();
    }
}
