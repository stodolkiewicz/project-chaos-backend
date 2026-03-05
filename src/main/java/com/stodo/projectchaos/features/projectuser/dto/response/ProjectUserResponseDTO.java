package com.stodo.projectchaos.features.projectuser.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProjectUserResponseDTO(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String googlePictureLink,
        Instant projectJoinDate
) {
}