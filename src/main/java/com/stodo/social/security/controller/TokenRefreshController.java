package com.stodo.social.security.controller;

import com.stodo.social.security.service.JwtService;
import com.stodo.social.security.model.TokenType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.stodo.social.security.config.SecurityConstants.JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS;

@RestController
@RequestMapping("/token")
public class TokenRefreshController {

    private final JwtService jwtService;

    public TokenRefreshController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            if (!jwtService.isValid(refreshToken) ||
                    !jwtService.extractTokenType(refreshToken).equals(TokenType.REFRESH.name())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = jwtService.extractEmail(refreshToken);
            String newAccessToken = jwtService.generateAccessToken(userEmail, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
