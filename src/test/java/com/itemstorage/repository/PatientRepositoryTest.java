package com.itemstorage.repository;

import com.itemstorage.entity.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class PatientRepositoryTest {

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
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Должен сохранить и найти пациента по номеру истории болезни")
    void testFindByMedicalCardNumber() {
        Patient patient = Patient.builder()
                .medicalCardNumber("ИБ-12345")
                .fullName("Тестовый Пациент")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        patientRepository.save(patient);

        Patient found = patientRepository.findByMedicalCardNumber("ИБ-12345").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getMedicalCardNumber()).isEqualTo("ИБ-12345");
        assertThat(found.getFullName()).isEqualTo("Тестовый Пациент");
    }
}