package com.stodo.projectchaos.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class VectorizationRequestedEvent {
    private final UUID attachmentId;
    private final UUID vectorizationOutboxId;
}
