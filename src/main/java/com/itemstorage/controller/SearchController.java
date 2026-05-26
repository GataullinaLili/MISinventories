package com.itemstorage.controller;

import com.itemstorage.entity.Patient;
import com.itemstorage.repository.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final PatientRepository patientRepository;

    private static final DateTimeFormatter DATE_FORMAT_DOT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_FORMAT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SearchController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public String searchPage(Model model) {
        return "search";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<Patient> searchApi(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String trimmedQuery = query.trim();

        // Параллельный поиск для улучшения производительности
        CompletableFuture<List<Patient>> byFio = CompletableFuture.supplyAsync(() ->
                patientRepository.findByFullNameContainingIgnoreCase(trimmedQuery));

        CompletableFuture<List<Patient>> byCard = CompletableFuture.supplyAsync(() ->
                patientRepository.findByMedicalCardNumberContaining(trimmedQuery));

        CompletableFuture<List<Patient>> byDate = CompletableFuture.supplyAsync(() ->
                searchByDate(trimmedQuery));

        // Объединяем результаты
        Set<Patient> resultSet = new LinkedHashSet<>();
        try {
            resultSet.addAll(byFio.get());
            resultSet.addAll(byCard.get());
            resultSet.addAll(byDate.get());
        } catch (Exception e) {
            // Fallback: последовательный поиск
            resultSet.addAll(patientRepository.findByFullNameContainingIgnoreCase(trimmedQuery));
            resultSet.addAll(patientRepository.findByMedicalCardNumberContaining(trimmedQuery));
            resultSet.addAll(searchByDate(trimmedQuery));
        }

        return new ArrayList<>(resultSet);
    }

    private List<Patient> searchByDate(String query) {
        String[] parts = query.split("\\s+");
        if (parts.length >= 2) {
            String fioPart = parts[0];
            String datePart = parts[parts.length - 1];

            LocalDate birthDate = null;
            try {
                birthDate = LocalDate.parse(datePart, DATE_FORMAT_DOT);
            } catch (Exception e1) {
                try {
                    birthDate = LocalDate.parse(datePart, DATE_FORMAT_ISO);
                } catch (Exception e2) {
                    return List.of();
                }
            }

            if (birthDate != null) {
                return patientRepository.findByFullNameAndBirthDate(fioPart, birthDate);
            }
        }
        return List.of();
    }
}