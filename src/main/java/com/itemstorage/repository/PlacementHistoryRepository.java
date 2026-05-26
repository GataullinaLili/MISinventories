package com.itemstorage.repository;

import com.itemstorage.entity.PlacementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PlacementHistoryRepository extends JpaRepository<PlacementHistory, Long> {

    List<PlacementHistory> findAllByOrderByPerformedAtDesc();

    // История по ID описи — с явной загрузкой inventory
    @Query("SELECT h FROM PlacementHistory h " +
            "LEFT JOIN FETCH h.inventory i " +
            "LEFT JOIN FETCH i.patient p " +
            "WHERE h.inventory.id = :inventoryId " +
            "ORDER BY h.performedAt DESC")
    List<PlacementHistory> findByInventoryIdOrderByPerformedAtDesc(@Param("inventoryId") Long inventoryId);

    long countByPerformedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h FROM PlacementHistory h " +
            "LEFT JOIN FETCH h.inventory i " +
            "LEFT JOIN FETCH i.patient p " +
            "WHERE p.medicalCardNumber = :cardNumber " +
            "ORDER BY h.performedAt DESC")
    List<PlacementHistory> findByPatientMedicalCard(@Param("cardNumber") String cardNumber);

    @Query("SELECT h FROM PlacementHistory h " +
            "LEFT JOIN FETCH h.inventory i " +
            "LEFT JOIN FETCH i.patient p " +
            "WHERE LOWER(h.performedBy) LIKE LOWER(CONCAT('%', :login, '%')) " +
            "ORDER BY h.performedAt DESC")
    List<PlacementHistory> findByPerformedByLogin(@Param("login") String login);
}