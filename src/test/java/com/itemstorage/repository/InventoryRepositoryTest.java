package com.itemstorage.repository;

import com.itemstorage.entity.Inventory;
import com.itemstorage.entity.Patient;
import com.itemstorage.entity.User;
import com.itemstorage.enums.InventoryStatus;
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
class InventoryRepositoryTest {

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
    private InventoryRepository inventoryRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Должен найти опись по ID с жадной загрузкой")
    void testFindByIdWithDetails() {
        User user = User.builder()
                .login("testuser")
                .passwordHash("hash")
                .fullName("Тест")
                .role(Role.ADMIN)
                .build();
        userRepository.save(user);

        Patient patient = Patient.builder()
                .medicalCardNumber("ИБ-99999")
                .fullName("Тестовый Пациент")
                .build();
        patientRepository.save(patient);

        Inventory inventory = new Inventory();
        inventory.setPatient(patient);
        inventory.setCreatedBy(user);
        inventory.setStatus(InventoryStatus.CREATED);
        inventory = inventoryRepository.save(inventory);

        var found = inventoryRepository.findByIdWithDetails(inventory.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPatient()).isNotNull();
        assertThat(found.get().getCreatedBy()).isNotNull();
    }
}