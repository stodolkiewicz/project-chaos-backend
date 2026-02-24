package com.stodo.projectchaos.integration.repository.config;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yml",
        "spring.liquibase.contexts=prod", // prod = only ddl
        "spring.jpa.hibernate.ddl-auto=none",
})
@ActiveProfiles("test")
@DataJpaTest
public class TestContainersBase {
}
