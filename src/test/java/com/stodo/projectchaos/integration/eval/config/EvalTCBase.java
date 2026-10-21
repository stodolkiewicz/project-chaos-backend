package com.stodo.projectchaos.integration.eval.config;

import com.stodo.projectchaos.integration.TestcontainersPostgresConfiguration;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yml",
        "spring.liquibase.contexts=dev", // dev = ddl + dml
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@ActiveProfiles("test")
@Tag("ai-eval")
@Import(TestcontainersPostgresConfiguration.class)
@SpringBootTest
public class EvalTCBase {
}
