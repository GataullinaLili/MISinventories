package com.itemstorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itemstorage.dto.InventoryRequest;
import com.itemstorage.dto.ItemRequest;
import com.itemstorage.entity.Inventory;
import com.itemstorage.entity.User;
import com.itemstorage.enums.InventoryStatus;
import com.itemstorage.enums.Role;
import com.itemstorage.repository.UserRepository;
import com.itemstorage.service.InventoryService;
import com.itemstorage.service.QrCodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryApiController.class)
class InventoryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private QrCodeService qrCodeService;

    @Test
    @WithMockUser(username = "TEST", roles = "RECEPTIONIST")
    @DisplayName("POST /api/inventory/create должен создать опись")
    void testCreateInventory_Success() throws Exception {
        // Подготовка mock пользователя (логин должен совпадать с username в @WithMockUser)
        User mockUser = User.builder()
                .id(1L)
                .login("TEST")
                .fullName("Тест")
                .role(Role.RECEPTIONIST)
                .build();

        // Подготовка mock описи
        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);

        // Настройка mock поведения
        when(userRepository.findByLogin("TEST")).thenReturn(Optional.of(mockUser));
        when(inventoryService.createInventory(any(InventoryRequest.class), eq(mockUser)))
                .thenReturn(mockInventory);

        // Создание DTO для запроса
        ItemRequest item = new ItemRequest();
        item.setName("Куртка");
        item.setQuantity(1);
        item.setDescription("Зимняя");

        InventoryRequest request = new InventoryRequest();
        request.setMedicalCardNumber("ИБ-12345");
        request.setItems(List.of(item));

        // Выполнение запроса
        mockMvc.perform(post("/api/inventory/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.inventoryId").value(1));
    }

    @Test
    @WithMockUser(username = "SKLAD", roles = "STOREKEEPER")
    @DisplayName("GET /api/inventory/{id} должен вернуть опись")
    void testGetInventory_Success() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setStatus(InventoryStatus.CREATED);

        when(inventoryService.getInventoryById(1L)).thenReturn(inventory);

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "SKLAD", roles = "STOREKEEPER")
    @DisplayName("GET /api/inventory/all должен вернуть список описей")
    void testGetAllInventories() throws Exception {
        when(inventoryService.getAllInventories()).thenReturn(List.of());

        mockMvc.perform(get("/api/inventory/all"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "SKLAD", roles = "STOREKEEPER")
    @DisplayName("GET /api/inventory/search должен выполнить поиск")
    void testSearchInventories() throws Exception {
        when(inventoryService.searchByPatientCardNumber("Иванов")).thenReturn(List.of());

        mockMvc.perform(get("/api/inventory/search?query=Иванов"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "SKLAD", roles = "STOREKEEPER")
    @DisplayName("POST /api/inventory/{id}/move должен переместить опись")
    void testMoveInventory() throws Exception {
        User mockUser = User.builder()
                .id(1L)
                .login("SKLAD")
                .fullName("Сотрудник Склада")
                .role(Role.STOREKEEPER)
                .build();

        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);

        when(userRepository.findByLogin("SKLAD")).thenReturn(Optional.of(mockUser));
        when(inventoryService.moveToCell(eq(1L), eq(2L), eq(mockUser))).thenReturn(mockInventory);

        String requestBody = "{\"cellId\": 2}";

        mockMvc.perform(post("/api/inventory/1/move")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "SKLAD", roles = "STOREKEEPER")
    @DisplayName("POST /api/inventory/{id}/issue должен выдать опись")
    void testIssueInventory() throws Exception {
        User mockUser = User.builder()
                .id(1L)
                .login("SKLAD")
                .fullName("Сотрудник Склада")
                .role(Role.STOREKEEPER)
                .build();

        Inventory mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setStatus(InventoryStatus.ISSUED);

        when(userRepository.findByLogin("SKLAD")).thenReturn(Optional.of(mockUser));
        when(inventoryService.issueInventory(eq(1L), eq(mockUser))).thenReturn(mockInventory);

        mockMvc.perform(post("/api/inventory/1/issue")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}