package com.stodo.social.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.Optional;

import static com.stodo.social.security.SecurityConstants.*;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractTokenFromHeader(request);
        String refreshToken = request.getHeader(REFRESH_TOKEN_HEADER);

        boolean accessTokenIsValid = jwtService.isValid(accessToken);
        boolean refreshTokenIsValid = jwtService.isValid(refreshToken);

        String accessTokenTokenTypeClaim = jwtService.extractTokenType(accessToken);
        String refreshTokenTokenTypeClaim = jwtService.extractTokenType(accessToken);

        // access token is fine - go ahead
        if (accessTokenIsValid && accessTokenTokenTypeClaim.equals(TokenType.ACCESS.name())) {
            filterChain.doFilter(request, response);
            return;
        }

        if(refreshTokenIsValid && refreshTokenTokenTypeClaim.equals(TokenType.REFRESH.name())) {
//            String jwtRefreshToken = jwtService.generateRefreshToken(oauth2User, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()))
                .orElse(null);
    }
}
