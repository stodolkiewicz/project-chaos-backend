package com.stodo.projectchaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity // enable method-level pre-authorization
@EnableAsync // enable @Async
@EnableScheduling
public class ProjectChaos {

	public static void main(String[] args) {
		SpringApplication.run(ProjectChaos.class, args);
	}
// https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html
//	https://spring.io/guides/tutorials/spring-boot-oauth2


	// todo: Refactor: use @CurrentUserId UUID userId in Controllers.

	// bucket4j for rate limiting???

//	https://github.com/pgvector/pgvector-java
}
