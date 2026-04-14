package com.stodo.projectchaos.kafka;

import com.stodo.projectchaos.features.vectorizationoutbox.VectorizationOutboxService;
import com.stodo.projectchaos.features.vectorizationoutbox.dto.service.VectorizationOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupVectorizationScheduler {
    private final KafkaVectorizationService kafkaVectorizationService;
    private final VectorizationOutboxService vectorizationOutboxService;

    @Value("${app.kafka.attachment-vectorization-max-retry-count}")
    private int maxRetryCount;

    @Scheduled(fixedDelay = 300, timeUnit = TimeUnit.SECONDS)
    public void send() {
        List<VectorizationOutbox> pending = vectorizationOutboxService.findPendingWithRetryCountLessThan(maxRetryCount);

        pending.forEach(outbox -> {
            boolean failed = false;
            try {
                failed = !kafkaVectorizationService.sendVectorizationMessage(
                        outbox.attachmentId(),
                        outbox.id()
                ).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while sending vectorization message for attachmentId: {}", outbox.attachmentId(), e);
                failed = true;
            } catch (Exception e) {
                failed = true;
            }

            if (failed) {
                boolean shouldSetStatusToFailed = outbox.retryCount() + 1 >= maxRetryCount;
                vectorizationOutboxService.markAsFailed(
                        outbox.id(),
                        shouldSetStatusToFailed
                );
                log.error("Increasing retryCount for vectorizationOutbox with id: {} to {}. Attachment id: {}",
                        outbox.id(),
                        outbox.retryCount() + 1,
                        outbox.attachmentId()
                );
                if(shouldSetStatusToFailed) {
                    log.error("Status of VectorizationOutbox with id: {} and attachment id: {} was set to FAILED",
                            outbox.id(),
                            outbox.attachmentId()
                    );
                }
            }
        });
    }
}
