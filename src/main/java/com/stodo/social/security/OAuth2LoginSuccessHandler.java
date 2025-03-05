package com.stodo.social.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.stodo.social.security.SecurityConstants.JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS;
import static com.stodo.social.security.SecurityConstants.JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final String frontendUrl;
    private final boolean secureCookie;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.cookie.secure}") boolean secureCookie
    ) {
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.secureCookie = secureCookie;
    }

    @Override
    public void onAuthenticationSuccess (
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String jwtAccessToken = jwtService.generateAccessToken(oauth2User, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);
        String jwtRefreshToken = jwtService.generateRefreshToken(oauth2User, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        jwtService.createAndAddSecureCookieToResponse(response, "access_token", jwtAccessToken, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);
        jwtService.createAndAddSecureCookieToResponse(response, "refresh_token", jwtRefreshToken, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        response.sendRedirect(frontendUrl + "/dashboard");
    }
}
