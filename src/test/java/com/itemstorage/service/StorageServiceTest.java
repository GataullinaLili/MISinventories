package com.itemstorage.service;

import com.itemstorage.entity.Storage;
import com.itemstorage.entity.StorageCell;
import com.itemstorage.enums.StorageType;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageCellRepository cellRepository;

    @InjectMocks
    private StorageService storageService;

    private Storage testStorage;

    @BeforeEach
    void setUp() {
        testStorage = new Storage();
        testStorage.setId(1L);
        testStorage.setName("Тестовый склад");
        testStorage.setStorageType(StorageType.LONG_TERM);
    }

    @Test
    @DisplayName("Должен успешно создать склад")
    void testCreateStorage_Success() {
        when(storageRepository.save(any(Storage.class))).thenReturn(testStorage);

        Storage result = storageService.createStorage("Тестовый склад", StorageType.LONG_TERM);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Тестовый склад");
        verify(storageRepository, times(1)).save(any(Storage.class));
    }

    @Test
    @DisplayName("Должен успешно создать ячейку")
    void testCreateCell_Success() {
        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage));

        StorageCell cell = new StorageCell();
        cell.setId(1L);
        cell.setName("А-01");
        when(cellRepository.save(any(StorageCell.class))).thenReturn(cell);

        StorageCell result = storageService.createCell("А-01", 1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("А-01");
    }

    @Test
    @DisplayName("Должен выбросить исключение при создании ячейки на несуществующем складе")
    void testCreateCell_StorageNotFound() {
        when(storageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storageService.createCell("А-01", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Склад не найден");
    }

    @Test
    @DisplayName("Должен успешно переименовать склад")
    void testRenameStorage_Success() {
        when(storageRepository.findById(1L)).thenReturn(Optional.of(testStorage));
        when(storageRepository.save(any(Storage.class))).thenReturn(testStorage);

        storageService.renameStorage(1L, "Новое название");

        assertThat(testStorage.getName()).isEqualTo("Новое название");
        verify(storageRepository).save(testStorage);
    }
}