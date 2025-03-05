package com.stodo.social.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import static com.stodo.social.security.SecurityConstants.OAUTH2_GOOGLE_CALLBACK_ENDPOINT;
import static com.stodo.social.security.SecurityConstants.OAUTH2_GOOGLE_LOGIN_START_ENDPOINT;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth ->
                auth.requestMatchers(
                                "/",
                                "/oauth2/**",
                                "/login/**",
                                OAUTH2_GOOGLE_LOGIN_START_ENDPOINT,
                                OAUTH2_GOOGLE_CALLBACK_ENDPOINT
                ).permitAll()
                // todo - delete it. Testing purposes
                .requestMatchers("/api/v1/demo/permitted").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(auth ->
                    auth.successHandler(oAuth2LoginSuccessHandler)
            );

        http.addFilterBefore(new JwtAuthenticationFilter(), OAuth2LoginAuthenticationFilter.class);

        return  http.build();
    }
}
