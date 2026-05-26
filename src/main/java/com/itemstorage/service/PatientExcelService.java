package com.itemstorage.service;

import com.itemstorage.entity.Inventory;
import com.itemstorage.entity.Patient;
import com.itemstorage.enums.InventoryStatus;
import com.itemstorage.repository.InventoryRepository;
import com.itemstorage.repository.PatientRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientExcelService {

    private final PatientRepository patientRepository;
    private final InventoryRepository inventoryRepository;

    public PatientExcelService(PatientRepository patientRepository,
                               InventoryRepository inventoryRepository) {
        this.patientRepository = patientRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Импорт пациентов из Excel-файла.
     * Ожидаемые колонки:
     *   A — Номер истории болезни (medicalCardNumber)
     *   B — ФИО пациента (fullName)
     *   C — Дата рождения (birthDate)
     * Первая строка пропускается как заголовок.
     */
    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            result.put("error", "Файл не выбран или пуст");
            return result;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String medicalCardNumber = getCellStringValue(row.getCell(0));
                    String fullName = getCellStringValue(row.getCell(1));
                    LocalDate birthDate = getCellDateValue(row.getCell(2));

                    if (medicalCardNumber.isEmpty() || fullName.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    if (patientRepository.findByMedicalCardNumber(medicalCardNumber).isPresent()) {
                        skipped++;
                        continue;
                    }

                    Patient patient = Patient.builder()
                            .medicalCardNumber(medicalCardNumber)
                            .fullName(fullName)
                            .birthDate(birthDate)
                            .createdAt(LocalDateTime.now())
                            .isDischarged(false)
                            .build();

                    patientRepository.save(patient);
                    imported++;

                } catch (Exception e) {
                    errors.add("Строка " + (i + 1) + ": " + e.getMessage());
                    skipped++;
                }
            }
        } catch (Exception e) {
            result.put("error", "Ошибка чтения файла: " + e.getMessage());
            return result;
        }

        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    /**
     * Экспорт всех пациентов в Excel-файл.
     * Колонки: Номер истории, ФИО, Дата рождения, Выписан, Дата выписки.
     */
    public byte[] exportToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Пациенты");

            // Стиль для заголовков
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Заголовок
            Row header = sheet.createRow(0);
            String[] columns = {"Номер истории", "ФИО", "Дата рождения", "Выписан", "Дата выписки"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Данные
            List<Patient> patients = patientRepository.findAll();
            int rowNum = 1;
            for (Patient patient : patients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(patient.getMedicalCardNumber());
                row.createCell(1).setCellValue(patient.getFullName());
                if (patient.getBirthDate() != null) {
                    row.createCell(2).setCellValue(patient.getBirthDate().toString());
                }
                row.createCell(3).setCellValue(
                        patient.getIsDischarged() != null && patient.getIsDischarged() ? "Да" : "Нет");
                if (patient.getDischargedAt() != null) {
                    row.createCell(4).setCellValue(
                            patient.getDischargedAt()
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                }
            }

            // Автоширина для всех колонок
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка экспорта пациентов в Excel", e);
        }
    }

    /**
     * Выписка пациентов из Excel-файла.
     * Ожидаемые колонки:
     *   A — Номер истории болезни (medicalCardNumber)
     * Первая строка пропускается как заголовок.
     *
     * При выписке проверяется наличие активных (не выданных) описей.
     * Пациент выписывается в любом случае, но при наличии активных описей
     * генерируется предупреждение.
     */
    @Transactional
    public Map<String, Object> dischargePatients(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int discharged = 0;
        int notFound = 0;
        int alreadyDischarged = 0;
        int hasActiveInventory = 0;
        List<String> warnings = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            result.put("error", "Файл не выбран или пуст");
            return result;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String medicalCardNumber = getCellStringValue(row.getCell(0));

                    if (medicalCardNumber.isEmpty()) {
                        continue;
                    }

                    Optional<Patient> patientOpt = patientRepository
                            .findByMedicalCardNumber(medicalCardNumber);

                    if (patientOpt.isEmpty()) {
                        notFound++;
                        continue;
                    }

                    Patient patient = patientOpt.get();

                    if (patient.getIsDischarged() != null && patient.getIsDischarged()) {
                        alreadyDischarged++;
                        continue;
                    }

                    // Проверяем наличие активных описей перед выпиской
                    List<Inventory> activeInventories = inventoryRepository
                            .findByPatientIdAndStatusNot(patient.getId(), InventoryStatus.ISSUED);

                    if (!activeInventories.isEmpty()) {
                        hasActiveInventory++;
                        String invNumbers = activeInventories.stream()
                                .map(inv -> "№" + inv.getId())
                                .collect(Collectors.joining(", "));
                        warnings.add("⚠ Пациент " + medicalCardNumber + " ("
                                + patient.getFullName() + ") выписан, но имеет "
                                + activeInventories.size() + " активных описей: "
                                + invNumbers);
                    }

                    // Выписываем пациента в любом случае
                    patient.setIsDischarged(true);
                    patient.setDischargedAt(LocalDateTime.now());
                    patientRepository.save(patient);
                    discharged++;

                } catch (Exception e) {
                    warnings.add("Ошибка в строке " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.put("error", "Ошибка чтения файла: " + e.getMessage());
            return result;
        }

        result.put("discharged", discharged);
        result.put("notFound", notFound);
        result.put("alreadyDischarged", alreadyDischarged);
        result.put("hasActiveInventory", hasActiveInventory);
        result.put("warnings", warnings);
        return result;
    }

    /**
     * Получение строкового значения ячейки Excel.
     * Поддерживает строковые и числовые типы.
     * Для числовых ячеек, содержащих дату, возвращает пустую строку
     * (даты обрабатываются отдельным методом).
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return ""; // Даты обрабатываются отдельно
                }
                // Форматируем число без десятичной части, если она нулевая
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * Получение значения даты из ячейки Excel.
     * Поддерживает:
     *   - NUMERIC ячейки с датой (внутренний формат Excel)
     *   - STRING ячейки с датой в форматах:
     *     dd.MM.yyyy, dd/MM/yyyy, yyyy-MM-dd, yyyy.MM.dd
     */
    private LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) return null;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                        "[dd.MM.yyyy][dd/MM/yyyy][yyyy-MM-dd][yyyy.MM.dd][dd.MM.yy][MM/dd/yyyy]");
                return LocalDate.parse(dateStr, formatter);
            }
        } catch (Exception e) {
            // Не удалось распознать дату — возвращаем null
        }
        return null;
    }
}