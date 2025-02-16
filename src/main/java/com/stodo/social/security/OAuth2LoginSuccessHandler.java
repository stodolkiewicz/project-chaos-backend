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
        String jwt = jwtService.generateToken(oauth2User);

        createAndAddSecureCookieToResponse(response, "jwt", jwt);
        // todo:
        //        createAndAddSecureCookieToResponse(response, "refresh_token", refreshToken);

        response.sendRedirect(frontendUrl + "/dashboard");
    }

    private void createAndAddSecureCookieToResponse(HttpServletResponse response, String cookieName, String cookieValue) {
        Cookie newCookie = new Cookie(cookieName, cookieValue);
        newCookie.setHttpOnly(true);
        newCookie.setSecure(true);

        newCookie.setPath("/");          // available in whole domain
        newCookie.setMaxAge(900);        // lives 15 minutes

        response.addCookie(newCookie);
    }
}
