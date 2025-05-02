package com.stodo.projectchaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ProjectChaos {

	public static void main(String[] args) {
		SpringApplication.run(ProjectChaos.class, args);
	}
// https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html
//	https://spring.io/guides/tutorials/spring-boot-oauth2


	// todo
	// Add project_id to task_priorities
	// Add project_id to labels
	// Testcontainers and some tests
	// tests for ProjectService.moveDefaultProjectToFront()
	// Figure out how to run different set of liquibase files for prod.
}
