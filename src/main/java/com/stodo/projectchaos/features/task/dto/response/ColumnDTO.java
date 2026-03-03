package com.stodo.projectchaos.features.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnDTO {
    private UUID id;
    private String name;
    private Short position;
}