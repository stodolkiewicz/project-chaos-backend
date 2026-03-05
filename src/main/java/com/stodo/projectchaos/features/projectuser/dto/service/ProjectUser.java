package com.stodo.projectchaos.features.projectuser.dto.service;

import java.time.Instant;
import java.util.UUID;

public record ProjectUser(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String googlePictureLink,
        Instant projectJoinDate
) {
}
