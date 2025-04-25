package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Versioned;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "labels")
public class LabelEntity extends Versioned {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "label", fetch = FetchType.LAZY)
    private Set<TaskLabelsEntity> taskLabels = new HashSet<>();
}
