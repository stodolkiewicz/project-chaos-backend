package com.stodo.projectchaos;

import com.stodo.projectchaos.security.service.JwtService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Generates and prints access token in the console for local development
@Component
@Profile("dev")
public class AccessTokenGeneratorRunner implements CommandLineRunner {

    private final JwtService jwtService;

    public AccessTokenGeneratorRunner(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void run(String... args) {
        String token = jwtService.generateAccessToken(
    "projectchaos32167@gmail.com",
            null,
            "Dawid",
            28800  // 8h
        );

        System.out.println("\n--- DEV ACCESS TOKEN ---");
        System.out.println(token);
        System.out.println("\n------------------------\n");
    }
}
