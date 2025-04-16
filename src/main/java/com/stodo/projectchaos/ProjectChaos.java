package com.stodo.projectchaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectChaos {

	public static void main(String[] args) {
		SpringApplication.run(ProjectChaos.class, args);
	}
// https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html
//	https://spring.io/guides/tutorials/spring-boot-oauth2

	// todo
	// Rest of Entities and liquibase for: columns, tasks, teams
	// Test data -> 002_dml.sql
	// Testcontainers and some tests
	// Figure out how to run different set of liquibase files for prod.
}
