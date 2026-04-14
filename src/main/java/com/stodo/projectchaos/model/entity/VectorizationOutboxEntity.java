package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.enums.VectorizationOutboxStatusEnum;
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
@Table(name = "vectorization_outbox")
@EntityListeners(AuditingEntityListener.class)
public class VectorizationOutboxEntity {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false, unique = true)
    private AttachmentEntity attachment;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private VectorizationOutboxStatusEnum status = VectorizationOutboxStatusEnum.PENDING;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdDate;

    @Column(name = "processed_at")
    private Instant processedAt;
}
