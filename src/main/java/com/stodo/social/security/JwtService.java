package com.stodo.social.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

import static com.stodo.social.security.SecurityConstants.*;

@Service
public class JwtService {
    // https://github.com/jwtk/jjwt?tab=readme-ov-file#what-is-a-json-web-token
    Logger log = LoggerFactory.getLogger(JwtService.class);

    private String secret;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
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
    private final Function<OAuth2User, JwtBuilder> createCommonTokenConfiguration = user -> Jwts.builder()
            .subject(user.getName())
            .issuedAt(new Date())
            .signWith(getSigningKey());

    public String generateAccessToken(OAuth2User user, long expirationInSeconds) {
        return createCommonTokenConfiguration.apply(user)
                .expiration(new Date(System.currentTimeMillis() + 1000 * expirationInSeconds)) // 15 min
                .claim(ROLE_CLAIM_NAME, getUserRole(user))
                .claim(TOKEN_TYPE_CLAIM_NAME, TokenType.ACCESS)
                .compact();
    }

    public String generateRefreshToken(OAuth2User user, long expirationInSeconds) {
        return createCommonTokenConfiguration.apply(user)
                .expiration(new Date(System.currentTimeMillis() + 1000 * expirationInSeconds)) // 15 min
                .claim(TOKEN_TYPE_CLAIM_NAME, TokenType.REFRESH)
                .compact();
    }


    private String getUserRole(OAuth2User user) {
        return ADMINS.contains(user.getName()) ?
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
        newCookie.setSecure(true);

        newCookie.setPath("/");          // available in whole domain
        newCookie.setMaxAge(maxAge);        // lives 15 minutes

        response.addCookie(newCookie);
    }
}
