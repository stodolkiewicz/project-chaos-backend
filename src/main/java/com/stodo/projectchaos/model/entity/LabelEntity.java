package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Versioned;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
}
