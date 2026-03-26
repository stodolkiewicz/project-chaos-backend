package com.stodo.projectchaos.storage.userlimit.dto.service;

import java.util.UUID;

public record UserStorageUsage(
        UUID id,
        UUID userId,
        Long usedBytes,
        Long limitBytes
) {
}