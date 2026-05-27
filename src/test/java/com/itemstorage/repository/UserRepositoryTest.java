package com.itemstorage.repository;

import com.itemstorage.entity.User;
import com.itemstorage.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Должен сохранить и найти пользователя по логину")
    void testFindByLogin() {
        User user = User.builder()
                .login("testuser")
                .passwordHash("hashedpassword")
                .fullName("Тестовый Пользователь")
                .role(Role.ADMIN)
                .active(true)
                .build();

        User saved = userRepository.save(user);

        User found = userRepository.findByLogin("testuser").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getLogin()).isEqualTo("testuser");
        assertThat(found.getFullName()).isEqualTo("Тестовый Пользователь");
    }

    @Test
    @DisplayName("Должен проверить существование логина")
    void testExistsByLogin() {
        User user = User.builder()
                .login("existinguser")
                .passwordHash("hash")
                .fullName("Существующий")
                .role(Role.STOREKEEPER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByLogin("existinguser");
        boolean notExists = userRepository.existsByLogin("nonexistent");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}