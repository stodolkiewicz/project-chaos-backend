package com.stodo.projectchaos.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task_comment_reads")
public class TaskCommentReadEntity {
    @EmbeddedId
    private TaskCommentReadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("user")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("task")
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;
}
