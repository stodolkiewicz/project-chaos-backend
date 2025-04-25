package com.stodo.projectchaos.security.config;

import java.util.List;

public final class SecurityConstants {
    public static final int JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS = 3600; // 1 hour
    public static final int JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS = 21600; // 6 hours

    public static final String ROLE_CLAIM_NAME = "role";
    public static final String TOKEN_TYPE_CLAIM_NAME = "tokenType";
    public static final String TOKEN_EXPIRATION_DATE_CLAIM_NAME = "exp";
    public static final String TOKEN_ISSUE_DATE_CLAIM_NAME = "iat";

    public static final List<String> ADMINS = List.of("admin@gmail.com");

    public static final String OAUTH2_GOOGLE_LOGIN_START_ENDPOINT = "/oauth2/authorization/google";
    // endpoint for exchanging authorization code for access token
    public static final String OAUTH2_GOOGLE_CALLBACK_ENDPOINT = "/login/oauth2/code/google";

    public static final String TOKEN_REFRESH_ENDPOINT = "/token/refresh";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token ";
}
