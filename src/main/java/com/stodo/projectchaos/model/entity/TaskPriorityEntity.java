package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Versioned;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task_priorities")
public class TaskPriorityEntity extends Versioned {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    ProjectEntity project;

    @Column(nullable = false)
    private Short priorityValue;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;
}
