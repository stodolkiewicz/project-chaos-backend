package com.stodo.projectchaos.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorizationEventListener {
    private final KafkaVectorizationService kafkaVectorizationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVectorizationRequested(VectorizationRequestedEvent event) {
        kafkaVectorizationService.sendVectorizationMessage(
                event.getAttachmentId(),
                event.getVectorizationOutboxId()
        );
    }
}