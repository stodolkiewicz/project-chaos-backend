package com.stodo.social.security.filter;

import com.stodo.social.security.service.JwtService;
import com.stodo.social.security.model.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.stodo.social.security.config.SecurityConstants.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromHeader(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (isValidTokenOfType(token, TokenType.ACCESS)) {
                String userEmail = jwtService.extractEmail(token);
                String userRole = jwtService.extractRole(token);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    List.of(new SimpleGrantedAuthority(userRole))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidTokenOfType(String token, TokenType expectedType) {
        return jwtService.isValid(token) &&
                jwtService.extractTokenType(token).equals(expectedType.name());
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()))
                .orElse(null);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getHeader(REFRESH_TOKEN_HEADER) == null && request.getHeader(AUTHORIZATION_HEADER) == null;
    }
}
