package com.stodo.projectchaos.security.controller;

import com.stodo.projectchaos.security.service.JwtService;
import com.stodo.projectchaos.security.model.TokenType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.stodo.projectchaos.security.config.SecurityConstants.JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS;

/*
Not used yet
*/
@RestController
@RequestMapping("/token")
public class TokenRefreshController {

    private final JwtService jwtService;

    public TokenRefreshController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /*
        Not used yet
    */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            if (!jwtService.isValid(refreshToken) ||
                    !jwtService.extractTokenType(refreshToken).equals(TokenType.REFRESH.name())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = jwtService.extractEmail(refreshToken);

            // todo - use generate access token with userId as param
             String newAccessToken = jwtService.generateAccessToken(userEmail, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
