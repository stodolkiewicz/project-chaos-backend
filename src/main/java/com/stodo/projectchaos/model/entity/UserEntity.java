package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import com.stodo.projectchaos.model.enums.RoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends Auditable {
    @Id
    private UUID id = UUID.randomUUID();

    @Email
    @Column(nullable = false, unique = true)
    private String email;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    private String googlePictureLink;

    private Instant lastLogin;

    @Builder.Default
    private boolean accountNonExpired = true;
    @Builder.Default
    private boolean accountNonLocked = true;
    @Builder.Default
    private boolean credentialsNonExpired = true;
    @Builder.Default
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_project_id")
    private ProjectEntity project;
}
