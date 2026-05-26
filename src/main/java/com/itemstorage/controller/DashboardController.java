package com.itemstorage.controller;

import com.itemstorage.entity.Inventory;
import com.itemstorage.service.AnalyticsService;
import com.itemstorage.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/analyst")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final AnalyticsService analyticsService;
    private final InventoryService inventoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("stats", analyticsService.getDashboardStats());
            model.addAttribute("dailyLoad", analyticsService.getDailyLoad(7));

            List<Inventory> dischargedAlerts = inventoryService.getActiveForDischargedPatients();
            model.addAttribute("dischargedAlerts", dischargedAlerts);

            return "analyst/dashboard";
        } catch (Exception e) {
            log.error("Ошибка загрузки дашборда", e);
            model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/inventories")
    public String inventories(Model model) {
        try {
            List<Inventory> inventories = inventoryService.getAllInventories();
            model.addAttribute("inventories", inventories);
            return "analyst/inventories";
        } catch (Exception e) {
            log.error("Ошибка загрузки описей для аналитика", e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/history")
    public String history() {
        return "analyst/history";
    }
}