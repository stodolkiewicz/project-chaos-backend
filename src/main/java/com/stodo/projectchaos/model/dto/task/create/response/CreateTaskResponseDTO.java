package com.stodo.projectchaos.model.dto.task.create.response;

import com.stodo.projectchaos.model.entity.TaskEntity;

import java.util.List;
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
        List<LabelDTO> labels
) {

    public static CreateTaskResponseDTO fromEntity(TaskEntity taskEntity) {
        return new CreateTaskResponseDTO(
                taskEntity.getId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getPositionInColumn(),
                taskEntity.getAssignee() == null ? null : taskEntity.getAssignee().getEmail(),
                taskEntity.getColumn() != null ? taskEntity.getColumn().getId() : null,
                taskEntity.getPriority() != null ? taskEntity.getPriority().getId() : null,
                taskEntity.getTaskLabels() != null
                        ? taskEntity.getTaskLabels().stream()
                        .map(taskLabel -> new LabelDTO(taskLabel.getLabel().getName(), taskLabel.getLabel().getColor()) )
                        .collect(Collectors.toList())
                        : List.of()
        );
    }
}
