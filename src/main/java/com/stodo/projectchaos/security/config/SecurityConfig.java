package com.stodo.projectchaos.security.config;

import com.stodo.projectchaos.security.filter.JwtAuthenticationFilter;
import com.stodo.projectchaos.security.handler.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

import static com.stodo.projectchaos.security.config.SecurityConstants.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

    public SecurityConfig(
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2AuthorizationRequestResolver = oAuth2AuthorizationRequestResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // when user tries to access secured resources, return 401
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    })
            )
            .authorizeHttpRequests(auth ->
                auth.requestMatchers(
                                "/",
                                "/oauth2/**",
                                "/login/**",
                                "/actuator/**",
                                OAUTH2_GOOGLE_LOGIN_START_ENDPOINT,
                                OAUTH2_GOOGLE_CALLBACK_ENDPOINT,
                                TOKEN_REFRESH_ENDPOINT
                ).permitAll()
                // todo - delete it. Testing purposes
                .requestMatchers("/api/v1/demo/permitted").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(auth ->
                    auth.successHandler(oAuth2LoginSuccessHandler)
                    .authorizationEndpoint(authorization ->
                            authorization.authorizationRequestResolver(oAuth2AuthorizationRequestResolver)
                    )
            );

        http.addFilterBefore(jwtAuthenticationFilter, OAuth2LoginAuthenticationFilter.class);

        return  http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // configuration applies to all paths
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}