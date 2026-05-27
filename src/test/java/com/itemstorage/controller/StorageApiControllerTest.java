package com.itemstorage.controller;

import com.itemstorage.entity.Storage;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.StorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StorageApiController.class)
class StorageApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageRepository storageRepository;

    @MockBean
    private StorageCellRepository storageCellRepository;

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    @DisplayName("GET /api/storages должен вернуть список складов")
    void testGetAllStorages() throws Exception {
        when(storageRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/storages"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STOREKEEPER")
    @DisplayName("GET /api/storage/{id}/free-cells должен вернуть список свободных ячеек")
    void testGetFreeCells() throws Exception {
        when(storageCellRepository.findFreeByStorageId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/storage/1/free-cells"))
                .andExpect(status().isOk());
    }
}