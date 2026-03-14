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
@Table(name = "ai_usage_logs")
@EntityListeners(AuditingEntityListener.class)
public class AIUsageLogsEntity {
    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false)
    private String modelId;

    @Column(nullable = false)
    private int promptTokens;

    @Column(nullable = false)
    private int completionTokens;

    @Column(nullable = false)
    private int totalTokens;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private Long latencyMs;

    @CreatedDate
    private Instant createdDate;

    // rest of fields from Auditable class - not useful
}
