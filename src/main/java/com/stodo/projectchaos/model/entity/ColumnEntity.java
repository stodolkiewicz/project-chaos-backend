package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import com.stodo.projectchaos.model.entity.superclass.Versioned;
import com.stodo.projectchaos.model.enums.ColumnColorEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "columns")
public class ColumnEntity extends Versioned {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    private Short position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ColumnColorEnum color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

}
