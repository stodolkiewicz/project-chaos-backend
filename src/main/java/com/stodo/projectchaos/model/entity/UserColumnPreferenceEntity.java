package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.enums.ColumnSoundEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_column_preferences")
public class UserColumnPreferenceEntity {
    @EmbeddedId
    private UserColumnPreferenceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("user")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("column")
    @JoinColumn(name = "column_id")
    private ColumnEntity column;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ColumnSoundEnum sound;

    @Column(nullable = false)
    private boolean confetti;
}
