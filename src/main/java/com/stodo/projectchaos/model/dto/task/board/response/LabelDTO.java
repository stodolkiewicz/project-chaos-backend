package com.stodo.projectchaos.model.dto.task.board.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabelDTO {
    private UUID id;
    private String name;
    private String color;
}