package com.stodo.projectchaos.features.task.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TasksResponseDTO {
    private UUID taskId;
    private String title;
    private String description;
    private Double positionInColumn;
    private PriorityDTO priority;
    private ColumnDTO column;
    private AssigneeDTO assignee;
    private List<LabelDTO> labels;
    private Instant createdDate;
}