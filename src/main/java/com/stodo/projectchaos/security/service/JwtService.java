package com.stodo.projectchaos.security.service;

import com.stodo.projectchaos.security.model.ROLE_NAME;
import com.stodo.projectchaos.security.model.TokenType;
import com.stodo.projectchaos.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import static com.stodo.projectchaos.security.config.SecurityConstants.*;

@Service
public class JwtService {
    // https://github.com/jwtk/jjwt?tab=readme-ov-file#what-is-a-json-web-token
    Logger log = LoggerFactory.getLogger(JwtService.class);

    private String secret;
    private String cookieDomain;
    private boolean cookieSecure;
    private UserService userService;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${app.security.cookie.domain}") String cookieDomain,
            @Value("${app.security.cookie.secure}") boolean cookieSecure,
            UserService userService
    ) {
        this.secret = secret;
        this.cookieDomain = cookieDomain;
        this.cookieSecure = cookieSecure;
        this.userService = userService;
    }

    // --------------------- Claim extraction -------------------------------------------------------------------------
    private final Function<String, Claims> getClaimsFromToken = token -> Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

    private <T> T getClaimsValue(String token, Function<Claims, T> claimFunction) {
        return getClaimsFromToken.andThen(claimFunction).apply(token);
    }

    public String extractEmail(String token) {
        return getClaimsValue(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return getClaimsValue(token, claims -> claims.get(TOKEN_TYPE_CLAIM_NAME, String.class));
    }

    public String extractRole(String token) {
        return getClaimsValue(token, claims -> claims.get(ROLE_CLAIM_NAME, String.class));
    }

    public Claims extractAllClaims(String token) {
        return getClaimsFromToken.apply(token);
    }

    public boolean isAccessToken(String token) {
        return extractTokenType(token).equals(TokenType.ACCESS.name());
    }

    public boolean isRefreshToken(String token) {
        return extractTokenType(token).equals(TokenType.REFRESH.name());
    }

    // --------------------- Token creation  --------------------------------------------------------------------------
    private final Function<String, JwtBuilder> createCommonTokenConfiguration = userEmail -> Jwts.builder()
            .subject(userEmail)
            .claim(ROLE_CLAIM_NAME, getUserRole(userEmail))
            .issuedAt(new Date())
            .signWith(getSigningKey());

    public String generateAccessToken(String userEmail, long expirationInSeconds) {
        return createCommonTokenConfiguration.apply(userEmail)
                .expiration(new Date(System.currentTimeMillis() + 1000 * expirationInSeconds)) // 15 min
                .claim(TOKEN_TYPE_CLAIM_NAME, TokenType.ACCESS)
                .compact();
    }

    public String generateAccessToken(String userEmail, String pictureUrl, String firstName, long expirationInSeconds) {
        UUID userId = userService.getUserIdByEmail(userEmail);

        return createCommonTokenConfiguration.apply(userEmail)
                .expiration(new Date(System.currentTimeMillis() + 1000 * expirationInSeconds)) // 15 min
                .claim(TOKEN_TYPE_CLAIM_NAME, TokenType.ACCESS)
                .claim("userId", userId)
                .claim("pictureUrl", pictureUrl)
                .claim("firstName", firstName)
                .compact();
    }

    public String generateRefreshToken(String userEmail, long expirationInSeconds) {
        return createCommonTokenConfiguration.apply(userEmail)
                .expiration(new Date(System.currentTimeMillis() + 1000 * expirationInSeconds)) // 6 hours
                .claim(TOKEN_TYPE_CLAIM_NAME, TokenType.REFRESH)
                .compact();
    }

    public String getUserRole(String userEmail) {
        return ADMINS.contains(userEmail) ?
                ROLE_NAME.ROLE_ADMIN.name() :
                ROLE_NAME.ROLE_USER.name();
    }

    // --------------------- Token validation  -------------------------------------------------------------------------
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            log.error("Invalid token: {}", String.valueOf(e));
            return false;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ----------- Cookie helper method --------------------------------------------------------------------------------
    public void createAndAddSecureCookieToResponse(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        Cookie newCookie = new Cookie(cookieName, cookieValue);
        newCookie.setHttpOnly(true);

        // secure cookie - forces browsers to only send this cookie via HTTPS
        newCookie.setSecure(cookieSecure);

        // allows cookie to "travel" through subdomains like auth.theprojectchaos.com and app.theprojectchaos.com
        newCookie.setDomain(cookieDomain);

        // allows all paths within subdomains
        newCookie.setPath("/");

        newCookie.setMaxAge(maxAge);

        response.addCookie(newCookie);
    }
}
