package com.itemstorage.controller;

import com.itemstorage.entity.Inventory;
import com.itemstorage.service.InventoryService;
import com.itemstorage.service.QrCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;
    private final QrCodeService qrCodeService;

    public InventoryController(InventoryService inventoryService, QrCodeService qrCodeService) {
        this.inventoryService = inventoryService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/inventories")
    public String inventoriesPage(@RequestParam(required = false, defaultValue = "false") boolean showAll,
                                  Model model) {
        try {
            List<Inventory> inventories;
            if (showAll) {
                inventories = inventoryService.getAllInventories();
            } else {
                inventories = inventoryService.getNotIssuedInventories();
            }

            if (inventories == null) {
                inventories = new ArrayList<>();
            }

            model.addAttribute("inventories", inventories);
            model.addAttribute("showAll", showAll);
        } catch (Exception e) {
            log.error("Ошибка загрузки описей: ", e);
            model.addAttribute("inventories", new ArrayList<>());
            model.addAttribute("showAll", false);
            model.addAttribute("error", "Ошибка загрузки описей: " + e.getMessage());
        }
        return "inventories";
    }

    @GetMapping("/inventory/{id}/download")
    public String downloadInventory(@PathVariable Long id, Model model) {
        try {
            Inventory inventory = inventoryService.getInventoryById(id);
            String qrCode = qrCodeService.generateQrCodeBase64(String.valueOf(id));
            model.addAttribute("inventory", inventory);
            model.addAttribute("qrCode", qrCode);
            log.info("Генерация PDF для описи №{}, QR содержит только ID: {}", id, id);
            return "inventory-print";
        } catch (Exception e) {
            log.error("Ошибка при скачивании описи {}: ", id, e);
            model.addAttribute("error", "Ошибка при скачивании описи: " + e.getMessage());
            return "error";
        }
    }
}