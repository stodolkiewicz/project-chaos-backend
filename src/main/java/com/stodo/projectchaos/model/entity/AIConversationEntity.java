package com.stodo.projectchaos.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AI_CONVERSATION")
@EntityListeners(AuditingEntityListener.class)
public class AIConversationEntity {
    @Id
    private String id;
    
    @Column(length = 255)
    private String title;

    @Builder.Default
    @Column(name = "conversation_has_title", nullable = false)
    private Boolean conversationHasTitle = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}