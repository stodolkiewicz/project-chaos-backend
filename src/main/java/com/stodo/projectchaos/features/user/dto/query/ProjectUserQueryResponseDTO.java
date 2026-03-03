package com.stodo.projectchaos.features.user.dto.query;

import java.time.Instant;

public record ProjectUserQueryResponseDTO(String email, String firstName, String lastName, String role, String googlePictureLink, Instant projectJoinDate) {
}
