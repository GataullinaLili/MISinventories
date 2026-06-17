package com.itemstorage.controller;

import com.itemstorage.dto.InventoryRequest;
import com.itemstorage.dto.ItemRequest;
import com.itemstorage.entity.Inventory;
import com.itemstorage.entity.User;
import com.itemstorage.repository.UserRepository;
import com.itemstorage.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    private static final Logger log = LoggerFactory.getLogger(ReceptionistController.class);

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public ReceptionistController(InventoryService inventoryService,
                                  UserRepository userRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/create")
    public String createInventoryPage() {
        return "receptionist/create-inventory";
    }

    @PostMapping("/create")
    public String createInventory(
            @ModelAttribute InventoryRequest request,
            @RequestParam(value = "itemPhotos", required = false) List<MultipartFile> itemPhotos,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        log.info("=== СОЗДАНИЕ ОПИСИ ===");
        log.info("Пациент: {}", request.getMedicalCardNumber());
        log.info("Вещей в запросе: {}", request.getItems() != null ? request.getItems().size() : 0);
        log.info("Фото: {}", itemPhotos != null ? itemPhotos.size() : 0);

        try {
            User currentUser = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (request.getMedicalCardNumber() == null || request.getMedicalCardNumber().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Не указан номер истории болезни");
                return "redirect:/receptionist/create";
            }

            List<ItemRequest> items = request.getItems();
            if (items == null || items.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Добавьте хотя бы одну вещь");
                return "redirect:/receptionist/create?medicalCardNumber=" + request.getMedicalCardNumber().trim();
            }

            // Фильтруем пустые фото
            List<MultipartFile> validPhotos = new ArrayList<>();
            if (itemPhotos != null) {
                for (MultipartFile photo : itemPhotos) {
                    if (photo != null && !photo.isEmpty() && photo.getSize() > 0) {
                        validPhotos.add(photo);
                    }
                }
            }

            Inventory inventory = inventoryService.createInventory(request, currentUser, validPhotos);

            redirectAttributes.addFlashAttribute("success",
                    "Опись №" + inventory.getId() + " успешно создана (вещей: " + items.size() + ")");
            return "redirect:/inventories";

        } catch (Exception e) {
            log.error("Ошибка создания описи", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/receptionist/create";
        }
    }

    @PostMapping("/api/create")
    @ResponseBody
    public Inventory createInventoryApi(
            @RequestPart("request") InventoryRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return inventoryService.createInventory(request, currentUser, photos);
    }
}