package com.stodo.projectchaos.model.dto.task.board.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssigneeDTO {
    private String email;
}
