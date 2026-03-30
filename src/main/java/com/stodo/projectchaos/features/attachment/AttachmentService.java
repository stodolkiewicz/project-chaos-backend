package com.stodo.projectchaos.features.attachment;

import com.stodo.projectchaos.exception.EntityNotFoundException;
import com.stodo.projectchaos.model.entity.AttachmentEntity;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<AttachmentInfo> findTaskAttachments(UUID projectId, UUID taskId) {
        return attachmentRepository.findByProjectIdAndTaskId(projectId, taskId);
    }

    public List<UUID> findByVectorStatus(VectorStatusEnum vectorStatus) {
        return attachmentRepository.findByVectorStatus(vectorStatus);
    }

    public AttachmentEntity findById(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> EntityNotFoundException.builder()
                        .identifier("id", attachmentId)
                        .entityType("AttachmentEntity")
                        .build());
    }
}