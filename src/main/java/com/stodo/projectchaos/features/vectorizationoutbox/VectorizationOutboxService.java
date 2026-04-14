package com.stodo.projectchaos.features.vectorizationoutbox;

import com.stodo.projectchaos.features.vectorizationoutbox.dto.mapper.VectorizationOutboxEntityMapper;
import com.stodo.projectchaos.features.vectorizationoutbox.dto.service.VectorizationOutbox;
import com.stodo.projectchaos.model.entity.AttachmentEntity;
import com.stodo.projectchaos.model.entity.VectorizationOutboxEntity;
import com.stodo.projectchaos.model.enums.VectorizationOutboxStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VectorizationOutboxService {

    private final VectorizationOutboxRepository repository;

    public VectorizationOutbox save(AttachmentEntity attachment) {
        VectorizationOutboxEntity entity = VectorizationOutboxEntity.builder()
                .attachment(attachment)
                .build();
        return VectorizationOutboxEntityMapper.INSTANCE.toVectorizationOutbox(repository.save(entity));
    }

    public List<VectorizationOutbox> findPendingWithRetryCountLessThan(int maxRetries) {
        return repository.findPendingWithRetryCountLessThan(VectorizationOutboxStatusEnum.PENDING, maxRetries)
                .stream()
                .map(VectorizationOutboxEntityMapper.INSTANCE::toVectorizationOutbox)
                .toList();
    }

    @Transactional
    public void markAsFailed(UUID id, boolean shouldSetFailedStatus) {
        repository.findById(id).ifPresent(entity -> {
            if (shouldSetFailedStatus) {
                entity.setStatus(VectorizationOutboxStatusEnum.FAILED);
            }
            entity.setRetryCount(entity.getRetryCount() + 1);
            entity.setProcessedAt(Instant.now());
            repository.save(entity);
        });
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
