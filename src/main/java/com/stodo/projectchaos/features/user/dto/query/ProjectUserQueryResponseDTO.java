package com.stodo.projectchaos.features.user.dto.query;

import java.time.Instant;
import java.util.UUID;

public record ProjectUserQueryResponseDTO(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String googlePictureLink,
        Instant projectJoinDate
) {
}
