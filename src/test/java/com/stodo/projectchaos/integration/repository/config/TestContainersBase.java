package com.stodo.projectchaos.integration.repository.config;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yml",
        "spring.liquibase.contexts=prod", // prod = only ddl
        "spring.jpa.hibernate.ddl-auto=none",
})
@ActiveProfiles("test")
@Tag("integration-test")
@DataJpaTest
@EnableJpaAuditing
public class TestContainersBase {
}
