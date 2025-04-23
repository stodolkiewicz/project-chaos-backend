package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project_backlog")
public class ProjectBacklogEntity extends Auditable {
    @EmbeddedId
    private ProjectBacklogId id;

    @ManyToOne
    @MapsId("project")
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne
    @MapsId("task")
    @JoinColumn(name = "task_id")
    private TaskEntity task;

}
