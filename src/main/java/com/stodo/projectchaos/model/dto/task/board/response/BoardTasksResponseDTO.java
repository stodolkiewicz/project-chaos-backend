package com.stodo.projectchaos.model.dto.task.board.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardTasksResponseDTO {
    private UUID taskId;
    private String title;
    private String description;
    private Double positionInColumn;
    private PriorityDTO priority;
    private ColumnDTO column;
    private AssigneeDTO assignee;
    private List<LabelDTO> labels;
}
