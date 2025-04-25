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

	// Endpoints for:
	// Endpoint getting basic info for project (/api/v1/projects/default) - done
	// Endpoint getting columns for the project (/api/v1/projects/{projectId}/columns)
	// Endpoint getting task labels (/api/v1/projects/{projectId}/tasks/labels)
	// Endpoint getting task priorities (/api/v1/projects/{projectId}/tasks/priorities)
	// Endpoint getting users assigned to tasks (/api/v1/projects/{projectId}/tasks/users)

	// Testcontainers and some tests
	// tests for ProjectService.moveDefaultProjectToFront()
	// Figure out how to run different set of liquibase files for prod.
}
