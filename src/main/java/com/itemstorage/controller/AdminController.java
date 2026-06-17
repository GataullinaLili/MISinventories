package com.itemstorage.controller;

import com.itemstorage.entity.Storage;
import com.itemstorage.entity.StorageCell;
import com.itemstorage.entity.User;
import com.itemstorage.enums.Role;
import com.itemstorage.enums.StorageType;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.StorageRepository;
import com.itemstorage.repository.UserRepository;
import com.itemstorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final StorageCellRepository storageCellRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/users")
    public String usersPage(Model model) {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String login,
                             @RequestParam String password,
                             @RequestParam String fullName,
                             @RequestParam Role role,
                             RedirectAttributes ra) {
        if (userRepository.existsByLogin(login)) {
            ra.addFlashAttribute("error", "Логин уже существует");
            return "redirect:/admin/users";
        }
        User user = User.builder()
                .login(login)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .active(true)
                .build();
        userRepository.save(user);
        ra.addFlashAttribute("success", "Пользователь создан");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam(required = false) String newLogin,
                             @RequestParam String fullName,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) String passwordConfirm,
                             RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (newLogin != null && !newLogin.equals(user.getLogin())) {
            if (userRepository.existsByLogin(newLogin)) {
                ra.addFlashAttribute("error", "Логин уже занят");
                return "redirect:/admin/users";
            }
            user.setLogin(newLogin);
        }

        user.setFullName(fullName);

        if (password != null && !password.isEmpty()) {
            if (!password.equals(passwordConfirm)) {
                ra.addFlashAttribute("error", "Пароли не совпадают");
                return "redirect:/admin/users";
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        userRepository.save(user);
        ra.addFlashAttribute("success", "Пользователь обновлён");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setActive(!user.getActive());
        userRepository.save(user);
        ra.addFlashAttribute("success", "Статус пользователя изменён");
        return "redirect:/admin/users";
    }


    @GetMapping("/storages")
    public String storagesPage(Model model) {
        List<Storage> storages = storageRepository.findAll();
        model.addAttribute("storages", storages);
        return "admin/storages";
    }

    @PostMapping("/storages/create")
    public String createStorage(@RequestParam String name,
                                @RequestParam StorageType storageType,
                                RedirectAttributes ra) {
        storageService.createStorage(name, storageType);
        ra.addFlashAttribute("success", "Склад добавлен");
        return "redirect:/admin/storages";
    }

    @PostMapping("/storages/{id}/delete")
    public String deleteStorage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            storageService.deleteStorage(id);
            ra.addFlashAttribute("success", "Склад удалён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/storages";
    }

    @PostMapping("/storages/{id}/rename")
    public String renameStorage(@PathVariable Long id,
                                @RequestParam String name,
                                RedirectAttributes ra) {
        try {
            storageService.renameStorage(id, name);
            ra.addFlashAttribute("success", "Склад переименован");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/storages";
    }


    @GetMapping("/cells")
    public String cellsPage(@RequestParam(required = false) Long storageId, Model model) {
        if (storageId != null) {
            model.addAttribute("cells", storageCellRepository.findByStorageIdWithFetch(storageId));
        } else {
            model.addAttribute("cells", storageCellRepository.findAllWithStorage());
        }
        model.addAttribute("storages", storageRepository.findAll());
        return "admin/cells";
    }

    @PostMapping("/cells/create")
    public String createCell(@RequestParam String name,
                             @RequestParam Long storageId,
                             RedirectAttributes ra) {
        try {
            storageService.createCell(name, storageId);
            ra.addFlashAttribute("success", "Ячейка добавлена");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cells";
    }

    @PostMapping("/cells/{id}/delete")
    public String deleteCell(@PathVariable Long id, RedirectAttributes ra) {
        try {
            storageService.deleteCell(id);
            ra.addFlashAttribute("success", "Ячейка удалена");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cells";
    }
}