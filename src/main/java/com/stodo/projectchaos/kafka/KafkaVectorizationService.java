package com.stodo.projectchaos.kafka;

import com.stodo.projectchaos.features.vectorizationoutbox.VectorizationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaVectorizationService {
    private final VectorizationOutboxService vectorizationOutboxService;
    @Value("${app.kafka.topic.attachment-vectorization-requested}")
    private String attachmentVectorizationRequestedTopicName;

    private final KafkaTemplate<Void, VectorizationMessage> kafkaTemplate;

    public CompletableFuture<Boolean> sendVectorizationMessage(UUID attachmentId, UUID vectorizationOutboxId) {
        VectorizationMessage vectorizationMessage = new VectorizationMessage(attachmentId);
        return kafkaTemplate.send(attachmentVectorizationRequestedTopicName, vectorizationMessage)
            .thenApply(result -> {
                log.info("Vectorization message for attachmentId: {}, was sent successfully!", attachmentId);
                vectorizationOutboxService.deleteById(vectorizationOutboxId);
                log.info("VectorizationOutbox id: {} deleted for attachmentId: {}, ", vectorizationOutboxId, attachmentId);
                return true;
            })
            .exceptionally(ex -> {
                log.error("Failed to send vectorization message for attachmentId: {}", attachmentId, ex);
                return false;
            });
    }
}
