package com.stodo.projectchaos.features.task.dto.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardTask {
    private UUID taskId;
    private String title;
    private String description;
    private Double positionInColumn;
    private Priority priority;
    private Column column;
    private Assignee assignee;
    private List<Label> labels;
}