package com.stodo.projectchaos.security.handler;

import com.stodo.projectchaos.features.invitation.dto.service.Invitation;
import com.stodo.projectchaos.features.project.ProjectService;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.ProjectRoleEnum;
import com.stodo.projectchaos.model.enums.RoleEnum;
import com.stodo.projectchaos.features.user.UserRepository;
import com.stodo.projectchaos.security.service.JwtService;
import com.stodo.projectchaos.features.invitation.InvitationService;
import com.stodo.projectchaos.storage.userlimit.UserLimitService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.stodo.projectchaos.security.config.SecurityConstants.JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS;
import static com.stodo.projectchaos.security.config.SecurityConstants.JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final String frontendDashboardUrl;
    private final UserRepository userRepository;
    private final InvitationService invitationService;
    private final ProjectService projectService;
    private final UserLimitService userLimitService;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            @Value("${app.frontend-dashboard-url}") String frontendDashboardUrl,
            UserRepository userRepository, InvitationService invitationService, ProjectService projectService, UserLimitService userLimitService
    ) {
        this.jwtService = jwtService;
        this.frontendDashboardUrl = frontendDashboardUrl;
        this.userRepository = userRepository;
        this.invitationService = invitationService;
        this.projectService = projectService;
        this.userLimitService = userLimitService;
    }

    @Override
    public void onAuthenticationSuccess (
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String userEmail = oauth2User.getAttribute("email");
        String pictureUrl = oauth2User.getAttribute("picture");
        String firstName = oauth2User.getAttribute("given_name");

        possiblyCreateUserAndAssignProjectsBasedOnInvitations(userEmail, oauth2User);

        String jwtAccessToken = jwtService.generateAccessToken(userEmail, pictureUrl, firstName, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);
        String jwtRefreshToken = jwtService.generateRefreshToken(userEmail, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

        jwtService.createAndAddSecureCookieToResponse(response, "access_token", jwtAccessToken, JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS);
        jwtService.createAndAddSecureCookieToResponse(response, "refresh_token", jwtRefreshToken, JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS);

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

    @Transactional
    void possiblyCreateUserAndAssignProjectsBasedOnInvitations(String userEmail, OAuth2User oauth2User) {
        if (!userRepository.existsByEmail(userEmail)) {
            saveNewUser(userEmail, oauth2User);
            convertInvitationsToProjectMemberships(userEmail);
        }
    }

    private void saveNewUser(String userEmail, OAuth2User oauth2User) {
        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setLastLogin(Instant.now());
        newUserEntity.setFirstName(oauth2User.getAttribute("given_name"));
        newUserEntity.setLastName(oauth2User.getAttribute("family_name"));
        newUserEntity.setEmail(userEmail);
        newUserEntity.setGooglePictureLink(oauth2User.getAttribute("picture"));
        newUserEntity.setRole(RoleEnum.valueOf(jwtService.getUserRole(userEmail)));

        UserEntity savedUser = userRepository.saveAndFlush(newUserEntity);
        userLimitService.createUserStorageUsage(savedUser.getId());
    }

    private void convertInvitationsToProjectMemberships(String userEmail) {
        List<Invitation> invitationsToProjects = invitationService.getInvitationsByEmail(userEmail);
        for(Invitation invitation: invitationsToProjects) {
            projectService.assignUserToProjectAndHandleUserDefaultProject(
                    invitation.projectId(),
                    invitation.email(),
                    ProjectRoleEnum.valueOf(invitation.role())
            );

            invitationService.deleteInvitation(invitation.id(), invitation.projectId());
        }
    }
}
