package com.stodo.projectchaos.model.dto.user.projectusers.query;

import java.time.Instant;

public record ProjectUserQueryResponseDTO(String email, String firstName, String lastName, String role, String googlePictureLink, Instant projectJoinDate) {
}
