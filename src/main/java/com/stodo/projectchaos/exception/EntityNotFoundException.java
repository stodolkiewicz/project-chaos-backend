package com.stodo.projectchaos.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@Builder
public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    @Singular("identifier")
    private final Map<String, Object> identifiers;

    @Override
    public String getMessage() {
        return "Entity not found: " + entityType + " with " + identifiers;
    }
}
