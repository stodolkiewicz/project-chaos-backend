package com.stodo.projectchaos.integration.repository;

import com.stodo.projectchaos.integration.repository.config.TestContainersBase;
import com.stodo.projectchaos.integration.repository.config.TestcontainersPostgresConfiguration;
import com.stodo.projectchaos.model.entity.UserEntity;
import com.stodo.projectchaos.model.enums.RoleEnum;
import com.stodo.projectchaos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;


@Import(TestcontainersPostgresConfiguration.class)
class UserRepositoryTCTest extends TestContainersBase {

    @Autowired
    UserRepository userRepository;

    @Test
    public void batchUpdateDefaultProjectsForUsersWithAlternatives_happyPath() {
        // todo: write a meaningful test
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("somemail@gmail.com");

        userRepository.save(userEntity);
    }
}
