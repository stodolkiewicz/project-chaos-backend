package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class TaskEntity extends Auditable {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String title;

    private String description;
    private Short positionInColumn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assigneeId;

    // optional = false -> hibernate will make sure that
    // this field is not null during merge or persist
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // nullable = false -> during hibernate creating tables
    @JoinColumn(name = "column_id", nullable = false)
    private ColumnEntity column;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "priority_id")
    private TaskPriorityEntity priority;
}
