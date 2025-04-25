package com.stodo.projectchaos.model.dto.response.boardtasks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriorityDTO {
    private UUID id;
    private Short priorityValue;
    private String name;
    private String color;
}