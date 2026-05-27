package com.itemstorage.service;

import com.itemstorage.dto.InventoryRequest;
import com.itemstorage.dto.ItemRequest;
import com.itemstorage.entity.*;
import com.itemstorage.enums.InventoryStatus;
import com.itemstorage.enums.Role;
import com.itemstorage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private StorageCellRepository cellRepository;

    @Mock
    private PlacementHistoryRepository historyRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private InventoryService inventoryService;

    private Patient testPatient;
    private User testUser;
    private StorageCell testCell;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setMedicalCardNumber("ИБ-10001");
        testPatient.setFullName("Тестовый Пациент");

        testUser = User.builder()
                .id(1L)
                .login("TEST")
                .fullName("Тестовый Сотрудник")
                .role(Role.STOREKEEPER)
                .build();

        Storage testStorage = new Storage();
        testStorage.setId(1L);
        testStorage.setName("Тестовый склад");

        testCell = new StorageCell();
        testCell.setId(1L);
        testCell.setName("А-01");
        testCell.setStorage(testStorage);
        testCell.setIsOccupied(false);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setStatus(InventoryStatus.CREATED);
        testInventory.setPatient(testPatient);
        testInventory.setCreatedBy(testUser);
    }

    @Test
    @DisplayName("Должен успешно создать опись")
    void testCreateInventory_Success() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Куртка");
        itemRequest.setQuantity(1);

        InventoryRequest request = new InventoryRequest();
        request.setMedicalCardNumber("ИБ-10001");
        request.setItems(List.of(itemRequest));

        when(patientRepository.findByMedicalCardNumber("ИБ-10001")).thenReturn(Optional.of(testPatient));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        Inventory result = inventoryService.createInventory(request, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при создании описи с несуществующим пациентом")
    void testCreateInventory_PatientNotFound() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Куртка");

        InventoryRequest request = new InventoryRequest();
        request.setMedicalCardNumber("ИБ-99999");
        request.setItems(List.of(itemRequest));

        when(patientRepository.findByMedicalCardNumber("ИБ-99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createInventory(request, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Должен успешно разместить опись в свободную ячейку")
    void testPlaceToStorage_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(cellRepository.findById(1L)).thenReturn(Optional.of(testCell));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(cellRepository.save(any(StorageCell.class))).thenReturn(testCell);

        Inventory result = inventoryService.placeToStorage(1L, 1L, testUser);

        assertThat(result.getStatus()).isEqualTo(InventoryStatus.PLACED);
        assertThat(result.getCell()).isEqualTo(testCell);
        assertThat(result.getPlacedByName()).isEqualTo("Тестовый Сотрудник");
        verify(cellRepository).save(any(StorageCell.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при размещении в занятую ячейку")
    void testPlaceToStorage_CellOccupied() {
        testCell.setIsOccupied(true);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(cellRepository.findById(1L)).thenReturn(Optional.of(testCell));

        assertThatThrownBy(() -> inventoryService.placeToStorage(1L, 1L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже занята");
    }

    @Test
    @DisplayName("Должен выбросить исключение при размещении несуществующей описи")
    void testPlaceToStorage_InventoryNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.placeToStorage(999L, 1L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    @DisplayName("Должен успешно выдать опись")
    void testIssueInventory_Success() {
        testInventory.setCell(testCell);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        Inventory result = inventoryService.issueInventory(1L, testUser);

        assertThat(result.getStatus()).isEqualTo(InventoryStatus.ISSUED);
        assertThat(result.getIssuedBy()).isEqualTo("Тестовый Сотрудник");
        verify(cellRepository).save(any(StorageCell.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при выдаче уже выданной описи")
    void testIssueInventory_AlreadyIssued() {
        testInventory.setStatus(InventoryStatus.ISSUED);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        assertThatThrownBy(() -> inventoryService.issueInventory(1L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже выдана");
    }

    @Test
    @DisplayName("Должен успешно переместить опись")
    void testMoveToCell_Success() {
        StorageCell newCell = new StorageCell();
        newCell.setId(2L);
        newCell.setName("Б-02");
        newCell.setIsOccupied(false);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(cellRepository.findById(2L)).thenReturn(Optional.of(newCell));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        Inventory result = inventoryService.moveToCell(1L, 2L, testUser);

        assertThat(result.getStatus()).isEqualTo(InventoryStatus.MOVED);
        verify(cellRepository, times(1)).save(any(StorageCell.class));
    }

    @Test
    @DisplayName("Должен получить опись по ID")
    void testGetInventoryById_Success() {
        when(inventoryRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testInventory));

        Inventory result = inventoryService.getInventoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Должен выбросить исключение при получении несуществующей описи")
    void testGetInventoryById_NotFound() {
        when(inventoryRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найдена");
    }
}