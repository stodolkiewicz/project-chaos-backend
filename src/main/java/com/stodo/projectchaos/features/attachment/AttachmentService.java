package com.stodo.projectchaos.features.attachment;

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
}