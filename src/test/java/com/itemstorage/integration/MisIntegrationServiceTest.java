package com.itemstorage.integration;

import com.itemstorage.dto.PatientDTO;
import com.itemstorage.service.MisIntegrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "mis.api.url=http://localhost:8081/mis/api")
class MisIntegrationServiceTest {

    @Autowired
    private MisIntegrationService misIntegrationService;

    @Test
    @DisplayName("Поиск пациентов должен вернуть список (или пустой список при ошибке)")
    void testSearchPatients() {
        var result = misIntegrationService.searchPatients("Иванов");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Получение всех пациентов должно вернуть список")
    void testGetAllPatients() {
        var result = misIntegrationService.getAllPatients();

        assertThat(result).isNotNull();
    }
}