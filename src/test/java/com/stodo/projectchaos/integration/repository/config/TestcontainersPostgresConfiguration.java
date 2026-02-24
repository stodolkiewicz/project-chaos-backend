package com.stodo.projectchaos.integration.repository.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersPostgresConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                .withDatabaseName("project-chaos")
                .withUsername("admin")
                .withPassword("admin")
                .withCreateContainerCmdModifier(cmd -> 
                    cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(5444), new ExposedPort(5432))
                    )
                );
    }

}
