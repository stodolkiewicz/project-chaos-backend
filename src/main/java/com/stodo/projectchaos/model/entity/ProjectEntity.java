package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "projects")
public class ProjectEntity extends Auditable {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 255)
    private String name;

    private String description;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<UserEntity> usersWithDefaultProject;
}
