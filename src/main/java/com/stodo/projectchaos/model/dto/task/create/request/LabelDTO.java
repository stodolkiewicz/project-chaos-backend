package com.stodo.projectchaos.model.dto.task.create.request;

import jakarta.validation.constraints.Size;

public record LabelDTO(@Size(min = 1, message = "Label name must have at least 1 character")
                       String name,
                       String color) {

}
