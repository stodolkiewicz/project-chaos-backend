package com.stodo.projectchaos.model.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserColumnPreferenceId implements Serializable {
    private UUID user;
    private UUID column;
}
