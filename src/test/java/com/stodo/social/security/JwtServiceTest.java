package com.stodo.social.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private static final String SECRET = "test_secret_key_minimum_32_chars_long_for_testing";
    private JwtService jwtService;

    @Mock
    private OAuth2User oauth2User;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        when(oauth2User.getName()).thenReturn("test@example.com");
    }

    @Test
    void shouldGenerateValidJwtToken() {
        // when
        String token = jwtService.generateToken(oauth2User);

        // then
        assertThat(token).isNotNull();
        String userEmail = jwtService.getUserEmail(token);
    }

}