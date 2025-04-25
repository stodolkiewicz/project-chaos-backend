package com.stodo.projectchaos.model.dto.response.boardtasks;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssigneeDTO {
    private UUID id;
    private String email;
}
