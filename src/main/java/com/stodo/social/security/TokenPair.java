package com.stodo.social.security;

public record TokenPair (
        String accessToken,
        String refreshToken
) {
}
