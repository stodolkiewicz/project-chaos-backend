package com.stodo.projectchaos.model.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class TaskLabelId implements Serializable {
    private UUID task;
    private UUID label;
}
