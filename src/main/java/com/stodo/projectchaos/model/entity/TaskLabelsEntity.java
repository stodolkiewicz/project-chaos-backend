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
@Table(name = "task_labels")
public class TaskLabelsEntity extends Auditable {
    @EmbeddedId
    private TaskLabelId id;

    @ManyToOne
    @MapsId("task")
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    @ManyToOne
    @MapsId("label")
    @JoinColumn(name = "label_id")
    private LabelEntity label;
}
