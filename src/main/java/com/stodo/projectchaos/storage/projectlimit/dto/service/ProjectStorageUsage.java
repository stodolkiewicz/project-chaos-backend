package com.stodo.projectchaos.storage.projectlimit.dto.service;

import java.util.UUID;

public record ProjectStorageUsage(
        UUID id,
        UUID projectId,
        Long usedBytes,
        Long limitBytes
) {
}