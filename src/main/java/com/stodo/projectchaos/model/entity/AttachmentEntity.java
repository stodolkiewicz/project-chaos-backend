package com.stodo.projectchaos.model.entity;

import com.stodo.projectchaos.model.entity.superclass.Auditable;
import com.stodo.projectchaos.model.enums.StorageStatusEnum;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attachments")
public class AttachmentEntity extends Auditable {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "file_name", nullable = false)
    private String fileName;


    @Column(name = "file_size_in_bytes")
    private Long fileSizeInBytes;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "vector_status", length = 20)
    private VectorStatusEnum vectorStatus = VectorStatusEnum.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_status", length = 20, nullable = false)
    private StorageStatusEnum storageStatus = StorageStatusEnum.SAVED;
}
