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
            @RequestParam("medicalCardNumber") String medicalCardNumber,
            @RequestParam(value = "items[0].name", required = false) List<String> itemNames,
            @RequestParam(value = "items[0].quantity", required = false) List<Integer> itemQuantities,
            @RequestParam(value = "items[0].description", required = false) List<String> itemDescriptions,
            @RequestParam(value = "itemPhotos", required = false) List<MultipartFile> itemPhotos,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        log.info("Создание описи: история={}, вещей={}, фото={}",
                medicalCardNumber,
                itemNames != null ? itemNames.size() : 0,
                itemPhotos != null ? itemPhotos.size() : 0);

        try {
            User currentUser = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (medicalCardNumber == null || medicalCardNumber.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Не указан номер истории болезни");
                return "redirect:/receptionist/create";
            }

            List<ItemRequest> items = new ArrayList<>();
            if (itemNames != null) {
                for (int i = 0; i < itemNames.size(); i++) {
                    String name = itemNames.get(i);
                    if (name == null || name.trim().isEmpty()) continue;

                    ItemRequest item = new ItemRequest();
                    item.setName(name.trim());
                    item.setQuantity(itemQuantities != null && i < itemQuantities.size() && itemQuantities.get(i) != null
                            ? itemQuantities.get(i) : 1);
                    item.setDescription(itemDescriptions != null && i < itemDescriptions.size() && itemDescriptions.get(i) != null
                            ? itemDescriptions.get(i).trim() : "");
                    items.add(item);
                }
            }

            if (items.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Добавьте хотя бы одну вещь");
                return "redirect:/receptionist/create?medicalCardNumber=" + medicalCardNumber.trim();
            }

            List<MultipartFile> validPhotos = new ArrayList<>();
            if (itemPhotos != null) {
                for (MultipartFile photo : itemPhotos) {
                    if (photo != null && !photo.isEmpty() && photo.getSize() > 0) {
                        validPhotos.add(photo);
                    }
                }
            }

            InventoryRequest invRequest = new InventoryRequest();
            invRequest.setMedicalCardNumber(medicalCardNumber.trim());
            invRequest.setItems(items);

            Inventory inventory = inventoryService.createInventory(invRequest, currentUser, validPhotos);

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