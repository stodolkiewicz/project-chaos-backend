package com.stodo.projectchaos.model.dto.response.boardtasks;

import jakarta.persistence.Column;
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
    @Column(length = 7)
    private String color;
}