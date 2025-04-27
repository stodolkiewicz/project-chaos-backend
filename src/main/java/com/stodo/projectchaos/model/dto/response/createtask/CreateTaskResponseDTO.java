package com.stodo.projectchaos.model.dto.response.createtask;

import com.stodo.projectchaos.model.entity.TaskEntity;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record CreateTaskResponseDTO(
        UUID id,
        String title,
        String description,
        Double positionInColumn,
        String assigneeEmail,
        UUID columnId,
        UUID priorityId,
        Set<String> labels
) {
    public static CreateTaskResponseDTO fromEntity(TaskEntity taskEntity) {
        return new CreateTaskResponseDTO(
                taskEntity.getId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getPositionInColumn(),
                taskEntity.getAssignee().getEmail(),
                taskEntity.getColumn() != null ? taskEntity.getColumn().getId() : null,
                taskEntity.getPriority() != null ? taskEntity.getPriority().getId() : null,
                taskEntity.getTaskLabels() != null
                        ? taskEntity.getTaskLabels().stream()
                        .map(taskLabel -> taskLabel.getLabel().getName())
                        .collect(Collectors.toSet())
                        : Set.of()
        );
    }
}
