package com.itemstorage.controller;

import com.itemstorage.dto.InventoryRequest;
import com.itemstorage.dto.ItemRequest;
import com.itemstorage.entity.Inventory;
import com.itemstorage.entity.User;
import com.itemstorage.enums.InventoryStatus;
import com.itemstorage.repository.UserRepository;
import com.itemstorage.service.InventoryService;
import com.itemstorage.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory API", description = "Управление описями вещей пациентов")
public class InventoryApiController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    @Operation(
            summary = "Создать новую опись",
            description = "Создает опись вещей для указанного пациента. Доступно ролям RECEPTIONIST и STOREKEEPER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Опись успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные или отсутствуют обязательные поля"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Пациент не найден")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createInventory(
            @Parameter(description = "Данные описи (номер карты + список вещей)", required = true)
            @RequestBody Map<String, Object> payload,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            User currentUser = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String medicalCardNumber = (String) payload.get("medicalCardNumber");
            if (medicalCardNumber == null || medicalCardNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Не указан номер истории болезни"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
            if (itemsData == null || itemsData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Добавьте хотя бы одну вещь"));
            }

            List<ItemRequest> items = itemsData.stream().map(data -> {
                ItemRequest item = new ItemRequest();
                item.setName((String) data.get("name"));
                item.setQuantity(data.get("quantity") != null ?
                        Integer.valueOf(data.get("quantity").toString()) : 1);
                item.setDescription((String) data.getOrDefault("description", ""));
                return item;
            }).toList();

            InventoryRequest request = new InventoryRequest();
            request.setMedicalCardNumber(medicalCardNumber.trim());
            request.setItems(items);

            Inventory inventory = inventoryService.createInventory(request, currentUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "inventoryId", inventory.getId(),
                    "status", inventory.getStatus().name(),
                    "message", "Опись №" + inventory.getId() + " создана"
            ));
        } catch (Exception e) {
            log.error("Ошибка создания описи через API", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Получить опись по ID", description = "Возвращает полную информацию об описи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Опись найдена"),
            @ApiResponse(responseCode = "404", description = "Опись не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getInventory(
            @Parameter(description = "ID описи", example = "1001", required = true)
            @PathVariable Long id) {
        try {
            Inventory inv = inventoryService.getInventoryById(id);
            return ResponseEntity.ok(inv);
        } catch (Exception e) {
            log.error("Ошибка получения описи #{}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Поиск описей", description = "Поиск по номеру медкарты или ФИО пациента")
    @GetMapping("/search")
    public ResponseEntity<?> searchInventories(
            @Parameter(description = "Поисковый запрос (номер карты или ФИО)", example = "ИБ-10001", required = true)
            @RequestParam String query,
            @Parameter(description = "Фильтр по статусу (CREATED, PLACED, MOVED, ISSUED)")
            @RequestParam(required = false) InventoryStatus status) {
        try {
            List<Inventory> inventories;
            if (status != null) {
                inventories = inventoryService.searchByQueryAndStatus(query, status);
            } else {
                inventories = inventoryService.searchByPatientCardNumber(query);
            }
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            log.error("Ошибка поиска описей", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Получить все описи", description = "Возвращает список всех описей")
    @GetMapping("/all")
    public ResponseEntity<?> getAllInventories() {
        try {
            List<Inventory> inventories = inventoryService.getAllInventories();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            log.error("Ошибка получения всех описей", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Описи выписанных пациентов", description = "Возвращает невыданные описи выписанных пациентов")
    @GetMapping("/discharged-alerts")
    public ResponseEntity<?> getDischargedAlerts() {
        try {
            List<Inventory> inventories = inventoryService.getActiveForDischargedPatients();
            return ResponseEntity.ok(inventories);
        } catch (Exception e) {
            log.error("Ошибка получения описей выписанных пациентов", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Переместить опись", description = "Перемещает опись в другую ячейку")
    @PostMapping("/{id}/move")
    public ResponseEntity<?> moveInventory(
            @Parameter(description = "ID описи", required = true) @PathVariable Long id,
            @Parameter(description = "ID целевой ячейки", required = true) @RequestBody Map<String, Object> payload,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            Long cellId = Long.valueOf(payload.get("cellId").toString());
            Inventory inventory = inventoryService.moveToCell(id, cellId, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Опись №" + id + " перемещена",
                    "cellName", inventory.getCell() != null ? inventory.getCell().getName() : "",
                    "storageName", inventory.getStorage() != null ? inventory.getStorage().getName() : ""
            ));
        } catch (Exception e) {
            log.error("Ошибка перемещения описи #{}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Выдать опись", description = "Отмечает опись как выданную пациенту")
    @PostMapping("/{id}/issue")
    public ResponseEntity<?> issueInventory(
            @Parameter(description = "ID описи", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            Inventory inventory = inventoryService.issueInventory(id, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Опись №" + id + " выдана",
                    "status", inventory.getStatus().name()
            ));
        } catch (Exception e) {
            log.error("Ошибка выдачи описи #{}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Получить QR-код описи", description = "Возвращает QR-код для описи в формате Base64")
    @GetMapping("/{id}/qr")
    public ResponseEntity<String> getInventoryQr(
            @Parameter(description = "ID описи", required = true) @PathVariable Long id) {
        try {
            Inventory inv = inventoryService.getInventoryById(id);
            String qrBase64 = qrCodeService.generateQrCodeBase64(String.valueOf(inv.getId()));
            return ResponseEntity.ok(qrBase64);
        } catch (Exception e) {
            log.error("Ошибка генерации QR-кода для описи #{}", id, e);
            return ResponseEntity.badRequest().body("");
        }
    }
}