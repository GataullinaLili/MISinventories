package com.itemstorage.controller;

import com.itemstorage.entity.PlacementHistory;
import com.itemstorage.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analyst/api")
@RequiredArgsConstructor
@Tag(name = "Analytics API", description = "Аналитика и история операций")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "История по ID описи", description = "Возвращает историю операций для указанной описи")
    @GetMapping("/history/inventory/{id}")
    @ResponseBody
    public List<PlacementHistory> historyByInventory(
            @Parameter(description = "ID описи", example = "1001", required = true)
            @PathVariable Long id) {
        return analyticsService.getHistoryByInventoryId(id);
    }

    @Operation(summary = "История по номеру медкарты", description = "Поиск истории по номеру медицинской карты")
    @GetMapping("/history/patient")
    @ResponseBody
    public List<PlacementHistory> historyByPatient(
            @Parameter(description = "Номер медицинской карты", example = "ИБ-10001", required = true)
            @RequestParam String cardNumber) {
        return analyticsService.getHistoryByMedicalCard(cardNumber);
    }

    @Operation(summary = "История по логину сотрудника", description = "Поиск операций, выполненных сотрудником")
    @GetMapping("/history/user")
    @ResponseBody
    public List<PlacementHistory> historyByUser(
            @Parameter(description = "Логин сотрудника", example = "SKLAD", required = true)
            @RequestParam String login) {
        return analyticsService.getHistoryByUser(login);
    }
}