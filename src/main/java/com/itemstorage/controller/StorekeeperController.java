package com.itemstorage.controller;

import com.itemstorage.entity.StorageCell;
import com.itemstorage.entity.User;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.UserRepository;
import com.itemstorage.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/storekeeper")
public class StorekeeperController {

    private static final Logger log = LoggerFactory.getLogger(StorekeeperController.class);

    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final StorageCellRepository storageCellRepository;

    public StorekeeperController(InventoryService inventoryService,
                                 UserRepository userRepository,
                                 StorageCellRepository storageCellRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
        this.storageCellRepository = storageCellRepository;
    }
    @GetMapping("/cells")
    public String cellsPage(Model model) {
        List<StorageCell> cells = storageCellRepository.findAllWithStorage();
        model.addAttribute("cells", cells);
        return "storekeeper/cells";
    }

    @PostMapping("/place")
    public String placeInventory(@RequestParam Long inventoryId,
                                 @RequestParam Long cellId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            inventoryService.placeToStorage(inventoryId, cellId, user);
            redirectAttributes.addFlashAttribute("success", "Опись №" + inventoryId + " размещена");
        } catch (Exception e) {
            log.error("Ошибка размещения описи №{}", inventoryId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventories";
    }

    @PostMapping("/move")
    public String moveInventory(@RequestParam Long inventoryId,
                                @RequestParam Long cellId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            inventoryService.moveToCell(inventoryId, cellId, user);
            redirectAttributes.addFlashAttribute("success", "Опись №" + inventoryId + " перемещена");
        } catch (Exception e) {
            log.error("Ошибка перемещения описи №{}", inventoryId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventories";
    }

    @PostMapping("/issue/{id}")
    public String issueInventory(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByLogin(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            inventoryService.issueInventory(id, user);
            redirectAttributes.addFlashAttribute("success", "Опись №" + id + " выдана");
        } catch (Exception e) {
            log.error("Ошибка выдачи описи №{}", id, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventories";
    }
}