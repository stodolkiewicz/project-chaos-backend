package com.stodo.social.entity;

import com.stodo.social.model.enums.RoleEnum;
import com.stodo.social.model.entity.UserEntity;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Tag("database")
@Tag("h2")
@DataJpaTest
@AutoConfigureTestDatabase // h2 by default
class UserEntityIT {

    @Autowired
    TestEntityManager em;

    UserEntity userEntity;

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String VALID_EMAIL = "john.doe@test.com";

    @BeforeEach
    public void setup() {
        userEntity = UserEntity.builder()
                .email(VALID_EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .role(RoleEnum.ROLE_USER)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    @Test
    void testUserEntity_whenValidUserDetailsAreProvided_shouldReturnStoredEntity() {
        // when
        UserEntity storedUserEntity = em.persistAndFlush(userEntity);

        // then
        assertNotNull(storedUserEntity.getId());
        assertEquals(FIRST_NAME, storedUserEntity.getFirstName());
        assertEquals(LAST_NAME, storedUserEntity.getLastName());
        assertEquals(VALID_EMAIL, storedUserEntity.getEmail());
    }

    @Test
    void testUserEntity_whenInvalidEmail_shouldThrowException() {
        // given
        userEntity.setEmail("invalid email");

        // when
        assertThrows(ConstraintViolationException.class ,() -> em.persistAndFlush(userEntity));
    }


}