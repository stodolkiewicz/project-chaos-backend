package com.stodo.projectchaos.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StorageLimitExceededException extends RuntimeException {
    private final String entityType;
    private final Long usedBytes;
    private final Long bytesToBeUploaded;
    private final Long limitInBytes;

    @Override
    public String getMessage() {
        return "Storage limit exceeded for entity type: " + entityType +
                ". Used bytes: " + usedBytes + ", number of bytes tried to be uploaded: " +
                bytesToBeUploaded + ". Limit in bytes: " + limitInBytes;
    }

}
