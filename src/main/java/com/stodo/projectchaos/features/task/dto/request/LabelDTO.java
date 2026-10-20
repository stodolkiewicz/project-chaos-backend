package com.stodo.projectchaos.features.task.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LabelDTO(@NotBlank(message = "Label name must not be blank")
                       String name,
                       @NotBlank(message = "Label color must not be blank")
                       String color) {

}
