package com.itemstorage.controller;

import com.itemstorage.dto.PatientDTO;
import com.itemstorage.entity.Patient;
import com.itemstorage.repository.PatientRepository;
import com.itemstorage.service.MisIntegrationService;
import com.itemstorage.service.PatientExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Контроллер для управления пациентами.
 * Обеспечивает импорт/экспорт, синхронизацию с МИС и поиск.
 */
@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient API", description = "Управление пациентами и интеграция с МИС")
public class PatientController {

    private final PatientExcelService patientExcelService;
    private final PatientRepository patientRepository;
    private final MisIntegrationService misIntegrationService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("[yyyy-MM-dd][dd.MM.yyyy][dd/MM/yyyy]");

    @Operation(summary = "Страница пациентов",
            description = "Отображает список всех пациентов с автоматической синхронизацией из МИС")
    @GetMapping
    public String patientsPage(Model model) {
        autoImportFromMis();

        List<Patient> allPatients = patientRepository.findAll();
        model.addAttribute("patients", allPatients);
        model.addAttribute("totalPatients", allPatients.size());

        return "patients";
    }

    @Operation(summary = "Импорт пациентов из Excel",
            description = "Загружает пациентов из Excel-файла (.xlsx)")
    @PostMapping("/import")
    public String importExcel(
            @Parameter(description = "Excel-файл с пациентами", required = true)
            @RequestParam("file") MultipartFile file,
            Model model) {
        Map<String, Object> result = patientExcelService.importFromExcel(file);
        model.addAttribute("result", result);
        model.addAttribute("patients", patientRepository.findAll());
        return "patients";
    }

    @Operation(summary = "Экспорт пациентов в Excel",
            description = "Скачивает всех пациентов в формате Excel")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = patientExcelService.exportToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=patients.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @Operation(summary = "Импорт одного пациента из МИС",
            description = "Импортирует пациента по номеру медицинской карты из внешней системы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пациент найден и импортирован",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Ошибка соединения с МИС")
    })
    @PostMapping("/import-single-from-mis")
    @ResponseBody
    public ResponseEntity<?> importSingleFromMis(
            @Parameter(description = "Номер медицинской карты",
                    example = "ИБ-10001",
                    required = true)
            @RequestParam String medicalCardNumber) {

        log.info("Запрос на импорт одного пациента из МИС: {}", medicalCardNumber);

        try {
            String cardNumber = medicalCardNumber.trim();

            Optional<Patient> existing = patientRepository.findByMedicalCardNumber(cardNumber);
            if (existing.isPresent()) {
                Patient p = existing.get();
                log.info("Пациент уже в базе: {} ({})", p.getFullName(), p.getMedicalCardNumber());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "exists", true,
                        "patientId", p.getId(),
                        "fullName", p.getFullName(),
                        "medicalCardNumber", p.getMedicalCardNumber(),
                        "discharged", p.getIsDischarged()
                ));
            }

            PatientDTO dto = misIntegrationService.getPatientByCard(cardNumber);
            if (dto == null) {
                log.warn("Пациент не найден в МИС: {}", cardNumber);
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Пациент с номером " + cardNumber + " не найден в МИС"
                ));
            }

            if (patientRepository.findByMedicalCardNumber(cardNumber).isPresent()) {
                Patient p = patientRepository.findByMedicalCardNumber(cardNumber).get();
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "exists", true,
                        "patientId", p.getId(),
                        "fullName", p.getFullName(),
                        "medicalCardNumber", p.getMedicalCardNumber(),
                        "discharged", p.getIsDischarged()
                ));
            }

            Patient patient = createPatientFromDTO(dto);
            patient = patientRepository.save(patient);

            log.info("Пациент успешно импортирован из МИС: {} ({})",
                    patient.getFullName(), patient.getMedicalCardNumber());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "exists", false,
                    "patientId", patient.getId(),
                    "fullName", patient.getFullName(),
                    "medicalCardNumber", patient.getMedicalCardNumber(),
                    "discharged", patient.getIsDischarged()
            ));

        } catch (Exception e) {
            log.error("Ошибка импорта одного пациента из МИС", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Ошибка соединения с МИС. Проверьте эмулятор на порту 8081."
            ));
        }
    }

    @Operation(summary = "Быстрый поиск в МИС",
            description = "Поиск пациентов в МИС для выпадающего списка")
    @GetMapping("/api/search-mis")
    @ResponseBody
    public List<PatientDTO> searchMis(
            @Parameter(description = "Поисковый запрос (минимум 2 символа)",
                    example = "Иванов",
                    required = true,
                    schema = @Schema(minLength = 2, maxLength = 100))
            @RequestParam String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return misIntegrationService.searchPatients(query.trim());
    }

    @Operation(summary = "Ручной импорт из МИС",
            description = "Импортирует пациентов из МИС по поисковому запросу")
    @PostMapping("/import-from-mis")
    public String importFromMis(
            @Parameter(description = "Поисковый запрос для МИС", required = true)
            @RequestParam String query,
            Model model) {
        log.info("Ручной импорт из МИС по запросу: {}", query);

        Map<String, Object> result = importPatientsByQuery(query);
        model.addAttribute("result", result);
        model.addAttribute("patients", patientRepository.findAll());
        return "patients";
    }

    // ==================== ПРИВАТНЫЕ МЕТОДЫ ====================

    private synchronized void autoImportFromMis() {
        try {
            log.info("=== Автоматическая синхронизация с МИС ===");

            List<PatientDTO> misPatients = misIntegrationService.getAllPatients();

            if (misPatients.isEmpty()) {
                log.warn("МИС не вернул пациентов. Проверьте, запущен ли эмулятор на порту 8081.");
                return;
            }

            int imported = 0;
            int skipped = 0;
            int updated = 0;

            for (PatientDTO dto : misPatients) {
                if (dto.getMedicalCardNumber() == null || dto.getMedicalCardNumber().isEmpty()) {
                    skipped++;
                    continue;
                }

                String cardNumber = dto.getMedicalCardNumber().trim();
                Optional<Patient> existingOpt = patientRepository.findByMedicalCardNumber(cardNumber);

                if (existingOpt.isPresent()) {
                    Patient existing = existingOpt.get();
                    boolean changed = false;

                    if (dto.getFullName() != null && !dto.getFullName().equals(existing.getFullName())) {
                        existing.setFullName(dto.getFullName());
                        changed = true;
                    }

                    if (existing.getBirthDate() == null && dto.getBirthDate() != null
                            && !dto.getBirthDate().isEmpty()) {
                        try {
                            existing.setBirthDate(LocalDate.parse(dto.getBirthDate(), DATE_FORMATTER));
                            changed = true;
                        } catch (Exception ignored) {}
                    }

                    if (dto.isDischarged() && !Boolean.TRUE.equals(existing.getIsDischarged())) {
                        existing.setIsDischarged(true);
                        if (dto.getDischargeDate() != null && !dto.getDischargeDate().isEmpty()) {
                            try {
                                existing.setDischargedAt(LocalDateTime.of(
                                        LocalDate.parse(dto.getDischargeDate(), DATE_FORMATTER),
                                        LocalTime.NOON));
                            } catch (Exception ignored) {}
                        }
                        changed = true;
                    }

                    if (changed) {
                        patientRepository.save(existing);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    Patient patient = createPatientFromDTO(dto);
                    patientRepository.save(patient);
                    imported++;
                }
            }

            log.info("=== Синхронизация завершена: новых={}, обновлено={}, пропущено={} ===",
                    imported, updated, skipped);

        } catch (Exception e) {
            log.error("Ошибка автосинхронизации с МИС: {}", e.getMessage());
        }
    }

    private Map<String, Object> importPatientsByQuery(String query) {
        Map<String, Object> result = new HashMap<>();
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try {
            List<PatientDTO> misPatients = misIntegrationService.searchPatients(query);

            if (misPatients.isEmpty()) {
                result.put("error", "Пациенты не найдены в МИС по запросу: " + query);
                return result;
            }

            for (PatientDTO dto : misPatients) {
                try {
                    if (dto.getMedicalCardNumber() == null || dto.getMedicalCardNumber().isEmpty()) {
                        skipped++;
                        continue;
                    }

                    String cardNumber = dto.getMedicalCardNumber().trim();

                    if (patientRepository.findByMedicalCardNumber(cardNumber).isPresent()) {
                        skipped++;
                        continue;
                    }

                    Patient patient = createPatientFromDTO(dto);
                    patientRepository.save(patient);
                    imported++;

                } catch (Exception e) {
                    errors.add(dto.getFullName() + ": " + e.getMessage());
                    skipped++;
                }
            }
        } catch (Exception e) {
            log.error("Ошибка импорта из МИС по запросу", e);
            result.put("error", "Ошибка соединения с МИС: " + e.getMessage());
        }

        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    private Patient createPatientFromDTO(PatientDTO dto) {
        Patient patient = new Patient();
        patient.setMedicalCardNumber(dto.getMedicalCardNumber() != null ?
                dto.getMedicalCardNumber().trim() : "");
        patient.setFullName(dto.getFullName() != null ?
                dto.getFullName().trim() : "Неизвестный");

        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            try {
                patient.setBirthDate(LocalDate.parse(dto.getBirthDate().trim(), DATE_FORMATTER));
            } catch (Exception e) {
                log.warn("Не удалось распарсить дату рождения '{}': {}",
                        dto.getBirthDate(), e.getMessage());
            }
        }

        patient.setIsDischarged(dto.isDischarged());

        if (dto.isDischarged() && dto.getDischargeDate() != null
                && !dto.getDischargeDate().isEmpty()) {
            try {
                patient.setDischargedAt(LocalDateTime.of(
                        LocalDate.parse(dto.getDischargeDate().trim(), DATE_FORMATTER),
                        LocalTime.NOON));
            } catch (Exception e) {
                log.warn("Не удалось распарсить дату выписки '{}': {}",
                        dto.getDischargeDate(), e.getMessage());
            }
        }

        patient.setCreatedAt(LocalDateTime.now());
        return patient;
    }
}