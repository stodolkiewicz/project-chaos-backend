package com.stodo.projectchaos.security.handler;

import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.RoleEnum;
import com.stodo.projectchaos.repository.UserRepository;
import com.stodo.projectchaos.security.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.stodo.projectchaos.security.config.SecurityConstants.JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS;
import static com.stodo.projectchaos.security.config.SecurityConstants.JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final String frontendDashboardUrl;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            @Value("${app.frontend-dashboard-url}") String frontendDashboardUrl,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.frontendDashboardUrl = frontendDashboardUrl;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess (
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String userEmail = oauth2User.getAttribute("email");

        String jwtAccessToken = jwtService.generateAccessToken(userEmail, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);
        String jwtRefreshToken = jwtService.generateRefreshToken(userEmail, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        jwtService.createAndAddSecureCookieToResponse(response, "access_token", jwtAccessToken, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);
        jwtService.createAndAddSecureCookieToResponse(response, "refresh_token", jwtRefreshToken, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        createUserIfNotExists(userEmail, oauth2User);
        invalidateSessionAndDeleteSessionCookie(request, response);

        response.sendRedirect(frontendDashboardUrl);
    }

    private void invalidateSessionAndDeleteSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        for (Cookie cookie : request.getCookies()) {
            if ("JSESSIONID".equals(cookie.getName())) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }
    }

    private void createUserIfNotExists(String userEmail, OAuth2User oauth2User) {
        if (!userRepository.existsByEmail(userEmail)) {
            UserEntity newUserEntity = new UserEntity();
            newUserEntity.setLastLogin(LocalDateTime.now());
            newUserEntity.setFirstName(oauth2User.getAttribute("given_name"));
            newUserEntity.setLastName(oauth2User.getAttribute("family_name"));
            newUserEntity.setEmail(userEmail);
            newUserEntity.setGooglePictureLink(oauth2User.getAttribute("picture"));
            newUserEntity.setRole(RoleEnum.valueOf(jwtService.getUserRole(userEmail)));

            userRepository.saveAndFlush(newUserEntity);
        }
    }
}
