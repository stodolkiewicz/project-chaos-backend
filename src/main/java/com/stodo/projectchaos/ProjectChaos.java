package com.stodo.projectchaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity // enable method-level pre-authorization
@EnableAsync // enable @Async
public class ProjectChaos {

	public static void main(String[] args) {
		SpringApplication.run(ProjectChaos.class, args);
	}
// https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html
//	https://spring.io/guides/tutorials/spring-boot-oauth2

	// TODO: Security Refactor (User ID in Session) - so that I do not have to make additional call to get userId
	// 1. JWT: Add 'userId' to Claims in JwtService during token generation.
	// 2. Auth: Create AppUserDetails extends User (with UUID userId field) for backward compatibility.
	// 3. Filter: In JwtFilter, instantiate AppUserDetails by extracting the ID from claims.


	// to do
//	Tabela projects:
//	id (UUID)
//	name (String)
//	storage_limit (Long) – np. 5368709120 dla 5GB.
//	current_storage_usage (Long) – suma bajtów.
//
//	Tabela attachments:
//	id (UUID)
//	project_id (UUID) – klucz obcy.
//	file_path (String) – ścieżka w GCS.
//			original_name (String).
//	content_type (String).
//	file_size (Long) – rozmiar w bajtach (potrzebny, żeby wiedzieć ile odjąć przy usuwaniu).
}
