package com.itemstorage.service;

import com.itemstorage.repository.InventoryRepository;
import com.itemstorage.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PatientExcelServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private PatientExcelService patientExcelService;

    @Test
    @DisplayName("Должен вернуть ошибку при пустом файле")
    void testImportFromExcel_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        var result = patientExcelService.importFromExcel(emptyFile);

        assertThat(result).containsKey("error");
        assertThat(result.get("error")).isEqualTo("Файл не выбран или пуст");
    }
}