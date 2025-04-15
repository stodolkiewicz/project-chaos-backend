package com.stodo.projectchaos.model.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class ProjectUserId implements Serializable {
    private UUID project;
    private UUID user;
}
