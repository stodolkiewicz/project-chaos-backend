package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "project_users")
public class ProjectUsersEntity {
    @EmbeddedId
    private ProjectUserId id;

    @ManyToOne
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