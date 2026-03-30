package com.stodo.projectchaos.embedding;

import com.stodo.projectchaos.features.attachment.AttachmentRepository;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EmbeddingScheduler {
    
    private final AttachmentRepository attachmentRepository;
    private final AttachmentEmbeddingService attachmentEmbeddingService;

    public EmbeddingScheduler(AttachmentRepository attachmentRepository, AttachmentEmbeddingService attachmentEmbeddingService) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentEmbeddingService = attachmentEmbeddingService;
    }

    @Scheduled(fixedDelay = 35000)
    public void scheduleEmbeddings() {
        List<UUID> vectorizationPendingAttachmentUUIDS = attachmentRepository.findByVectorStatus(VectorStatusEnum.PENDING);
        for(UUID attachmentUUID: vectorizationPendingAttachmentUUIDS) {
            attachmentEmbeddingService.embedAttachment(attachmentUUID);
        }
    }
}
