package com.itemstorage.service;

import com.itemstorage.dto.DashboardStats;
import com.itemstorage.entity.*;
import com.itemstorage.enums.InventoryStatus;
import com.itemstorage.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final InventoryRepository inventoryRepository;
    private final PlacementHistoryRepository placementHistoryRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public AnalyticsService(InventoryRepository inventoryRepository,
                            PlacementHistoryRepository placementHistoryRepository,
                            UserRepository userRepository,
                            PatientRepository patientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.placementHistoryRepository = placementHistoryRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    public DashboardStats getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        long operationsToday = placementHistoryRepository.countByPerformedAtBetween(todayStart, todayEnd);
        long createdToday = inventoryRepository.countCreatedBetween(todayStart, todayEnd);
        long issuedToday = inventoryRepository.countIssuedBetween(todayStart, todayEnd);

        List<Inventory> active = inventoryRepository.findByStatusNot(InventoryStatus.ISSUED);

        long totalCells = inventoryRepository.countTotalCells();
        Long occupiedNow = inventoryRepository.countOccupiedCellsAtDate(LocalDate.now());
        long occupied = occupiedNow != null ? occupiedNow : 0;
        long free = totalCells - occupied;

        return DashboardStats.builder()
                .operationsToday(operationsToday + createdToday + issuedToday)
                .activeInventories(active.size())
                .issuedToday((int) issuedToday)
                .dischargedCount((int) patientRepository.countByIsDischargedTrue())
                .occupiedCells(occupied)
                .freeCells(free)
                .totalCells(totalCells)
                .build();
    }

    public List<Map<String, Object>> getDailyLoad(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            long created = inventoryRepository.countCreatedBetween(start, end);
            long issued = inventoryRepository.countIssuedBetween(start, end);
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date.toString());
            dayStats.put("createdCount", created);
            dayStats.put("issuedCount", issued);
            result.add(dayStats);
        }
        return result;
    }

    public Map<String, Object> getCellOccupancy(int days) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dailyOccupancy = new ArrayList<>();
        long totalCells = inventoryRepository.countTotalCells();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long occupiedCells = inventoryRepository.countOccupiedCellsAtDate(date);
            long occupied = occupiedCells != null ? occupiedCells : 0;
            long free = totalCells - occupied;
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date.toString());
            dayStats.put("occupied", occupied);
            dayStats.put("free", free);
            dayStats.put("total", totalCells);
            double percent = totalCells > 0 ? Math.round((occupied * 100.0 / totalCells) * 10.0) / 10.0 : 0;
            dayStats.put("percent", percent);
            dailyOccupancy.add(dayStats);
        }
        result.put("dailyOccupancy", dailyOccupancy);
        result.put("totalCells", totalCells);
        Long currentOccupied = inventoryRepository.countOccupiedCellsAtDate(today);
        long occ = currentOccupied != null ? currentOccupied : 0;
        result.put("currentOccupied", occ);
        result.put("currentFree", totalCells - occ);
        return result;
    }

    public List<PlacementHistory> getAllHistory() {
        return placementHistoryRepository.findAllByOrderByPerformedAtDesc();
    }

    public List<PlacementHistory> getHistoryByInventoryId(Long inventoryId) {
        return placementHistoryRepository.findByInventoryIdOrderByPerformedAtDesc(inventoryId);
    }

    public List<PlacementHistory> getHistoryByMedicalCard(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return List.of();
        }
        return placementHistoryRepository.findByPatientMedicalCard(cardNumber.trim());
    }

    public List<PlacementHistory> getHistoryByUser(String userLogin) {
        if (userLogin == null || userLogin.trim().isEmpty()) {
            return List.of();
        }
        return placementHistoryRepository.findByPerformedByLogin(userLogin.trim());
    }

    public List<Inventory> getActiveForDischargedPatients() {
        return inventoryRepository.findActiveForDischargedPatients();
    }
}