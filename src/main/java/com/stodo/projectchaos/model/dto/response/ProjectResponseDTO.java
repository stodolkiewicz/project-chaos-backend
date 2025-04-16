package com.stodo.projectchaos.model.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private boolean defaultProject;
}
