package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project_users")
public class ProjectUsersEntity extends Auditable {
    @EmbeddedId
    private ProjectUserId id;

    @ManyToOne(cascade = {})
    @MapsId("project")
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne
    @MapsId("user")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role")
    private ProjectRoleEnum projectRole;

}