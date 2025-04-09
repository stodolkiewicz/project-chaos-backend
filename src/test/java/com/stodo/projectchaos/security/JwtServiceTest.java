package com.stodo.projectchaos.security;

import com.stodo.projectchaos.security.model.ROLE_NAME;
import com.stodo.projectchaos.security.model.TokenType;
import com.stodo.projectchaos.security.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static com.stodo.projectchaos.security.config.SecurityConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private static final String SECRET = "test_secret_key_minimum_32_chars_long_for_testing";

    private JwtService jwtService;

    private static final String USER_EMAIL = "test@example.com";
    private static final String ADMIN_EMAIL = ADMINS.getFirst();

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @DisplayName("Generate Access Token sanity check")
    @Test
    void shouldGenerateValidAccessToken() {
        // given

        // when
        String token = jwtService.generateAccessToken(USER_EMAIL, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);

        // then
        String userEmail = jwtService.extractEmail(token);
        String tokenType = jwtService.extractTokenType(token);
        Claims claims = jwtService.extractAllClaims(token);
        Date expirationDate = new Date(claims.get(TOKEN_EXPIRATION_DATE_CLAIM_NAME, Long.class));
        Date issueDate = new Date(claims.get(TOKEN_ISSUE_DATE_CLAIM_NAME, Long.class));

        assertThat(token).isNotNull();
        assertEquals(USER_EMAIL, userEmail);
        assertEquals(TokenType.ACCESS.name(), tokenType);
        assertTrue(expirationDate.after(issueDate));
    }

    @DisplayName("Access Token - ROLE_USER")
    @Test
    void testGenerateAccessToken_whenUser_shouldGenerateValidAccessTokenWithUserRole() {
        // given

        // when
        String token = jwtService.generateAccessToken(USER_EMAIL, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);

        // then
        String userEmail = jwtService.extractEmail(token);
        String tokenType = jwtService.extractTokenType(token);
        Claims claims = jwtService.extractAllClaims(token);
        Date expirationDate = new Date(claims.get(TOKEN_EXPIRATION_DATE_CLAIM_NAME, Long.class));
        Date issueDate = new Date(claims.get(TOKEN_ISSUE_DATE_CLAIM_NAME, Long.class));
        String role = claims.get(ROLE_CLAIM_NAME, String.class);

        assertThat(token).isNotNull();
        assertEquals(USER_EMAIL, userEmail);
        assertEquals(TokenType.ACCESS.name(), tokenType);
        assertTrue(expirationDate.after(issueDate));
        assertEquals(ROLE_NAME.ROLE_USER.name(), role);
    }

    @DisplayName("Access Token - ROLE_ADMIN")
    @Test
    void testGenerateAccessToken_whenAdmin_shouldGenerateValidAccessTokenWithUserRole() {
        // given

        // when
        String token = jwtService.generateAccessToken(ADMIN_EMAIL, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);


        // then
        String role = jwtService.extractAllClaims(token).get(ROLE_CLAIM_NAME, String.class);
        assertEquals(ROLE_NAME.ROLE_ADMIN.name(), role);
    }

    @DisplayName("Generate Refresh Token sanity check")
    @Test
    void shouldGenerateValidRefreshToken() {
        // given

        // when
        String token = jwtService.generateRefreshToken(USER_EMAIL, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        // then
        String userEmail = jwtService.extractEmail(token);
        String tokenType = jwtService.extractTokenType(token);
        Claims claims = jwtService.extractAllClaims(token);
        Date expirationDate = new Date(claims.get(TOKEN_EXPIRATION_DATE_CLAIM_NAME, Long.class));
        Date issueDate = new Date(claims.get(TOKEN_ISSUE_DATE_CLAIM_NAME, Long.class));
        String role = claims.get(ROLE_CLAIM_NAME, String.class);

        assertThat(token).isNotNull();
        assertEquals(ROLE_NAME.ROLE_USER.name(), role);
        assertEquals(USER_EMAIL, userEmail);
        assertEquals(TokenType.REFRESH.name(), tokenType);
        assertTrue(expirationDate.after(issueDate));
    }

    @DisplayName("Validate token - valid")
    @Test
    void testIsValid_whenValid_shouldReturnTrue() {
        // given
        String token = jwtService.generateRefreshToken(USER_EMAIL, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        // when
        boolean isValidToken = jwtService.isValid(token);

        // then
        assertTrue(isValidToken);
    }

    @DisplayName("Validate token - invalid token String")
    @Test
    void testIsValid_whenInvalid_shouldReturnFalse() {
        // given
        String token = jwtService.generateRefreshToken(USER_EMAIL, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);
        token = "a" + token;

        // when
        boolean isValidToken = jwtService.isValid(token);

        // then
        assertFalse(isValidToken);
    }

    @DisplayName("Validate token - expired")
    @Test
    void testIsValid_whenTokenExpired_shouldReturnFalse() throws InterruptedException {
        // given
        String token = jwtService.generateRefreshToken(USER_EMAIL, 1);
        Thread.sleep(1100);

        // when
        boolean isValidToken = jwtService.isValid(token);

        // then
        assertFalse(isValidToken);
    }

}