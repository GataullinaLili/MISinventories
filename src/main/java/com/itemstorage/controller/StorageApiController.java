package com.itemstorage.controller;

import com.itemstorage.entity.Storage;
import com.itemstorage.entity.StorageCell;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.StorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Storage API", description = "Управление складами и ячейками")
public class StorageApiController {

    private final StorageRepository storageRepository;
    private final StorageCellRepository storageCellRepository;

    @Operation(summary = "Получить все склады", description = "Возвращает список всех складов, отсортированный по названию")
    @GetMapping("/storages")
    public List<Storage> getAllStorages() {
        List<Storage> storages = storageRepository.findAll();
        return storages.stream()
                .sorted(Comparator.comparing(Storage::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Свободные ячейки склада", description = "Возвращает список свободных ячеек указанного склада, отсортированный по названию")
    @GetMapping("/storage/{storageId}/free-cells")
    public List<StorageCell> getFreeCells(
            @Parameter(description = "ID склада", example = "2", required = true)
            @PathVariable Long storageId) {
        List<StorageCell> cells = storageCellRepository.findFreeByStorageId(storageId);
        return cells.stream()
                .sorted(Comparator.comparing(StorageCell::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}