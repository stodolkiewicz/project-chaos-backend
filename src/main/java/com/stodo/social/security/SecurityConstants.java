package com.stodo.social.security;

import java.util.List;

public final class SecurityConstants {
    public static final int JWT_ACCESS_TOKEN_EXPIRATION_IN_SECONDS = 900; // 15 minutes
    public static final int JWT_REFRESH_TOKEN_EXPIRATION_IN_SECONDS = 21600; // 6 hours

    public static final String ROLE_CLAIM_NAME = "role";
    public static final String TOKEN_TYPE_CLAIM_NAME = "tokenType";

    public static final List<String> ADMINS = List.of("stodoo@gmail.com");
}
