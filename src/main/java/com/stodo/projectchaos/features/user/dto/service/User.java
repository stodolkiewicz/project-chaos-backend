package com.stodo.projectchaos.features.user.dto.service;

import java.util.UUID;

public record User(
        UUID id,
        String email,
        String firstName,
        String lastName
) {
}