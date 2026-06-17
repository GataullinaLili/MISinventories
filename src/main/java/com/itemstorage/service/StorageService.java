package com.itemstorage.service;

import com.itemstorage.entity.Storage;
import com.itemstorage.entity.StorageCell;
import com.itemstorage.enums.StorageType;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.StorageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StorageService {

    private final StorageRepository storageRepository;
    private final StorageCellRepository cellRepository;

    public StorageService(StorageRepository storageRepository,
                          StorageCellRepository cellRepository) {
        this.storageRepository = storageRepository;
        this.cellRepository = cellRepository;
    }

    @Cacheable(value = "storages", unless = "#result == null || #result.isEmpty()")
    public List<Storage> getAllStorages() {
        System.out.println("=== ЗАПРОС К БД: getAllStorages ===");
        return storageRepository.findAll();
    }

    @CacheEvict(value = "storages", allEntries = true)
    @Transactional
    public Storage createStorage(String name, StorageType storageType) {
        Storage storage = new Storage();
        storage.setName(name);
        storage.setStorageType(storageType);
        return storageRepository.save(storage);
    }

    @Transactional
    public StorageCell createCell(String name, Long storageId) {
        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new RuntimeException("Склад не найден"));


        StorageCell cell = new StorageCell();
        cell.setName(name);
        cell.setStorage(storage);
        cell.setIsOccupied(false);

        evictCellCaches();
        return cellRepository.save(cell);
    }

    @Cacheable(value = "freeCells", key = "#storageId", unless = "#result == null || #result.isEmpty()")
    public List<StorageCell> getFreeCells(Long storageId) {
        System.out.println("=== ЗАПРОС К БД: getFreeCells для склада " + storageId);
        return cellRepository.findFreeByStorageId(storageId);
    }

    @Cacheable(value = "cellsByStorage", key = "#storageId", unless = "#result == null || #result.isEmpty()")
    public List<StorageCell> getCellsByStorage(Long storageId) {
        System.out.println("=== ЗАПРОС К БД: getCellsByStorage для склада " + storageId);
        return cellRepository.findByStorageIdWithFetch(storageId);
    }

    @CacheEvict(value = "storages", allEntries = true)
    @Transactional
    public void deleteStorage(Long storageId) {
        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new RuntimeException("Склад не найден"));
        if (!storage.getCells().isEmpty()) {
            throw new RuntimeException("Нельзя удалить склад с ячейками");
        }
        storageRepository.delete(storage);
        evictCellCaches();
    }

    @CacheEvict(value = "storages", allEntries = true)
    @Transactional
    public void renameStorage(Long storageId, String newName) {
        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new RuntimeException("Склад не найден"));
        storage.setName(newName);
        storageRepository.save(storage);
    }

    @Transactional
    public void deleteCell(Long cellId) {
        StorageCell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new RuntimeException("Ячейка не найдена"));
        if (Boolean.TRUE.equals(cell.getIsOccupied())) {
            throw new RuntimeException("Нельзя удалить занятую ячейку");
        }
        cellRepository.delete(cell);
        evictCellCaches();
    }

    @CacheEvict(value = {"freeCells", "cellsByStorage"}, allEntries = true)
    public void evictCellCaches() {
    }
}