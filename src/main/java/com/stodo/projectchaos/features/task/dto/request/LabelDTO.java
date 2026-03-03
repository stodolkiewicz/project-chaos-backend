package com.stodo.projectchaos.features.task.dto.request;

import jakarta.validation.constraints.Size;

public record LabelDTO(@Size(min = 1, message = "Label name must have at least 1 character")
                       String name,
                       String color) {

}
