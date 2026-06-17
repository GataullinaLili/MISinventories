package com.itemstorage.service;

import com.itemstorage.dto.InventoryRequest;
import com.itemstorage.dto.ItemRequest;
import com.itemstorage.entity.*;
import com.itemstorage.enums.InventoryStatus;
import com.itemstorage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final PatientRepository patientRepository;
    private final StorageCellRepository cellRepository;
    private final PlacementHistoryRepository historyRepository;
    private final FileStorageService fileStorageService;

    public InventoryService(InventoryRepository inventoryRepository,
                            PatientRepository patientRepository,
                            StorageCellRepository cellRepository,
                            PlacementHistoryRepository historyRepository,
                            FileStorageService fileStorageService) {
        this.inventoryRepository = inventoryRepository;
        this.patientRepository = patientRepository;
        this.cellRepository = cellRepository;
        this.historyRepository = historyRepository;
        this.fileStorageService = fileStorageService;
    }

    @CacheEvict(value = {"inventories", "activeInventories", "notIssuedInventories", "activeForDischarged"}, allEntries = true)
    public Inventory createInventory(InventoryRequest request, User createdBy,
                                     List<MultipartFile> photos) {
        log.info("=== СОЗДАНИЕ ОПИСИ ===");
        log.info("Пациент: {}", request.getMedicalCardNumber());
        log.info("Вещей: {}", request.getItems() != null ? request.getItems().size() : 0);
        log.info("Фото: {}", photos != null ? photos.size() : 0);

        Patient patient = patientRepository.findByMedicalCardNumber(request.getMedicalCardNumber())
                .orElseThrow(() -> new RuntimeException(
                        "Пациент с номером " + request.getMedicalCardNumber() + " не найден"));

        Inventory inventory = new Inventory();
        inventory.setPatient(patient);
        inventory.setStatus(InventoryStatus.CREATED);
        inventory.setCreatedBy(createdBy);
        inventory.setCreatedAt(LocalDateTime.now());

        List<ItemRequest> items = request.getItems();

        List<ItemRequest> sortedItems = items.stream()
                .sorted(Comparator.comparing(ItemRequest::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        log.info("Сортировка вещей по наименованию выполнена");

        for (int i = 0; i < sortedItems.size(); i++) {
            ItemRequest itemReq = sortedItems.get(i);

            Item item = new Item();
            item.setName(itemReq.getName());
            item.setQuantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1);
            item.setDescription(itemReq.getDescription());
            item.setInventory(inventory);

            if (photos != null && i < photos.size()) {
                MultipartFile photo = photos.get(i);
                if (photo != null && !photo.isEmpty() && photo.getSize() > 0) {
                    log.info("Сохранение фото для '{}': {} ({} bytes)",
                            item.getName(), photo.getOriginalFilename(), photo.getSize());
                    String photoPath = fileStorageService.storeFile(photo);
                    if (photoPath != null) {
                        item.setPhotoPath(photoPath);
                        log.info("Фото сохранено: {}", photoPath);
                    }
                }
            }

            inventory.getItems().add(item);
        }

        inventory = inventoryRepository.save(inventory);
        log.info("Опись №{} создана (вещей: {})", inventory.getId(), sortedItems.size());

        PlacementHistory history = new PlacementHistory();
        history.setInventory(inventory);
        history.setAction("CREATED");
        history.setPerformedBy(createdBy.getFullName() + " (" + createdBy.getLogin() + ")");
        history.setPerformedAt(LocalDateTime.now());
        historyRepository.save(history);

        return inventory;
    }

    public Inventory createInventory(InventoryRequest request, User createdBy) {
        return createInventory(request, createdBy, null);
    }

    @CacheEvict(value = {"inventories", "activeInventories", "notIssuedInventories", "activeForDischarged", "freeCells", "cellsByStorage"}, allEntries = true)
    public Inventory placeToStorage(Long inventoryId, Long cellId, User performedBy) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Опись №" + inventoryId + " не найдена"));

        if (inventory.getStatus() == InventoryStatus.ISSUED) {
            throw new RuntimeException("Нельзя разместить выданную опись");
        }
        if (cellId == null) {
            throw new RuntimeException("Не указана ячейка для размещения");
        }

        StorageCell cell = cellRepository.findById(cellId)
                .orElseThrow(() -> new RuntimeException("Ячейка не найдена"));

        if (Boolean.TRUE.equals(cell.getIsOccupied())) {
            throw new RuntimeException("Ячейка '" + cell.getName() + "' уже занята");
        }

        String previousCell = null;
        if (inventory.getCell() != null) {
            previousCell = inventory.getCell().getName();
            freeCell(inventory.getCell());
        }

        inventory.setStorage(cell.getStorage());
        inventory.setCell(cell);
        inventory.setStatus(InventoryStatus.PLACED);
        inventory.setPlacedByName(performedBy.getFullName());
        inventory.setPlacedAt(LocalDateTime.now());
        cell.setIsOccupied(true);

        cellRepository.save(cell);
        inventoryRepository.save(inventory);
        saveHistory(inventory, "PLACED", performedBy, previousCell, cell.getName());

        log.info("Опись №{} размещена в '{}' сотрудником {}", inventoryId, cell.getName(), performedBy.getFullName());
        return inventory;
    }

    @CacheEvict(value = {"inventories", "activeInventories", "notIssuedInventories", "activeForDischarged", "freeCells", "cellsByStorage"}, allEntries = true)
    public Inventory moveToCell(Long inventoryId, Long cellId, User performedBy) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Опись №" + inventoryId + " не найдена"));

        if (inventory.getStatus() == InventoryStatus.ISSUED) {
            throw new RuntimeException("Нельзя переместить выданную опись");
        }

        StorageCell newCell = cellRepository.findById(cellId)
                .orElseThrow(() -> new RuntimeException("Ячейка не найдена"));

        if (inventory.getCell() != null && inventory.getCell().getId().equals(newCell.getId())) {
            throw new RuntimeException("Опись уже в этой ячейке");
        }

        if (Boolean.TRUE.equals(newCell.getIsOccupied())) {
            throw new RuntimeException("Ячейка '" + newCell.getName() + "' уже занята");
        }

        String previousCell = null;
        if (inventory.getCell() != null) {
            previousCell = inventory.getCell().getName();
            freeCell(inventory.getCell());
        }

        inventory.setStorage(newCell.getStorage());
        inventory.setCell(newCell);
        inventory.setStatus(InventoryStatus.MOVED);
        inventory.setMovedByName(performedBy.getFullName());
        inventory.setMovedAt(LocalDateTime.now());
        inventory.setPreviousCell(previousCell);
        newCell.setIsOccupied(true);

        cellRepository.save(newCell);
        inventoryRepository.save(inventory);
        saveHistory(inventory, "MOVED", performedBy, previousCell, newCell.getName());

        log.info("Опись №{} перемещена из '{}' в '{}'", inventoryId, previousCell, newCell.getName());
        return inventory;
    }

    @CacheEvict(value = {"inventories", "activeInventories", "notIssuedInventories", "activeForDischarged", "freeCells", "cellsByStorage"}, allEntries = true)
    public Inventory issueInventory(Long inventoryId, User performedBy) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Опись №" + inventoryId + " не найдена"));

        if (inventory.getStatus() == InventoryStatus.ISSUED) {
            throw new RuntimeException("Опись №" + inventoryId + " уже выдана");
        }

        String previousCell = null;
        if (inventory.getCell() != null) {
            previousCell = inventory.getCell().getName();
            freeCell(inventory.getCell());
            inventory.setCell(null);
            inventory.setStorage(null);
        }

        inventory.setStatus(InventoryStatus.ISSUED);
        inventory.setIssuedAt(LocalDateTime.now());
        inventory.setIssuedBy(performedBy.getFullName());
        inventoryRepository.save(inventory);
        saveHistory(inventory, "ISSUED", performedBy, previousCell, null);

        log.info("Опись №{} выдана сотрудником {}", inventoryId, performedBy.getFullName());
        return inventory;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "inventories", key = "#id", unless = "#result == null")
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Опись №" + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "inventories", unless = "#result == null || #result.isEmpty()")
    public List<Inventory> getAllInventories() {
        List<Inventory> inventories = inventoryRepository.findAllWithDetails();
        for (Inventory inv : inventories) {
            if (inv.getItems() != null) {
                inv.getItems().sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return inventories;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "activeInventories", unless = "#result == null || #result.isEmpty()")
    public List<Inventory> getActiveInventories() {
        List<Inventory> inventories = inventoryRepository.findActiveInventoriesWithDetails();
        for (Inventory inv : inventories) {
            if (inv.getItems() != null) {
                inv.getItems().sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return inventories;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "notIssuedInventories", unless = "#result == null || #result.isEmpty()")
    public List<Inventory> getNotIssuedInventories() {
        List<Inventory> inventories = inventoryRepository.findNotIssuedWithDetails();
        for (Inventory inv : inventories) {
            if (inv.getItems() != null) {
                inv.getItems().sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return inventories;
    }

    @Transactional(readOnly = true)
    public List<Inventory> searchByPatientCardNumber(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        List<Inventory> inventories = inventoryRepository.searchByCardOrFio(query.trim());
        for (Inventory inv : inventories) {
            if (inv.getItems() != null) {
                inv.getItems().sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return inventories;
    }

    @Transactional(readOnly = true)
    public List<Inventory> searchByQueryAndStatus(String query, InventoryStatus status) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        List<Inventory> inventories = inventoryRepository.searchByQueryAndStatus(query.trim(), status);
        for (Inventory inv : inventories) {
            if (inv.getItems() != null) {
                inv.getItems().sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return inventories;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "activeForDischarged", unless = "#result == null || #result.isEmpty()")
    public List<Inventory> getActiveForDischargedPatients() {
        List<Inventory> inventories = inventoryRepository.findActiveForDischargedPatients();
        for (Inventory inv : inventories) {
            if (inv.getItems() != null) {
                inv.getItems().sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return inventories;
    }

    private void freeCell(StorageCell cell) {
        if (cell != null) {
            cell.setIsOccupied(false);
            cellRepository.save(cell);
        }
    }

    private void saveHistory(Inventory inventory, String action, User performedBy,
                             String previousCell, String newCell) {
        PlacementHistory history = new PlacementHistory();
        history.setInventory(inventory);
        history.setCell(inventory.getCell());
        history.setAction(action);
        history.setPerformedBy(performedBy.getFullName() + " (" + performedBy.getLogin() + ")");
        history.setPerformedAt(LocalDateTime.now());
        history.setPreviousCell(previousCell);
        history.setNewCell(newCell);
        historyRepository.save(history);

        log.info("История: опись №{}, действие={}, сотрудник={}",
                inventory.getId(), action, performedBy.getFullName());
    }
}