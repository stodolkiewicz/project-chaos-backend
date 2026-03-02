package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "invitations", uniqueConstraints = {
    @UniqueConstraint(name = "unique_invitation", columnNames = {"email", "project_id"})
})
public class InvitationEntity extends Auditable {
    @Id
    private UUID id = UUID.randomUUID();

    @Email
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invited_by", nullable = false)
    private UserEntity invitedBy;
}