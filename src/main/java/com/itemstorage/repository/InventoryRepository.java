package com.itemstorage.repository;

import com.itemstorage.entity.Inventory;
import com.itemstorage.enums.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByStatus(InventoryStatus status);
    List<Inventory> findByStatusNot(InventoryStatus status);
    List<Inventory> findByPatientId(Long patientId);
    List<Inventory> findByPatientIdAndStatusNot(Long patientId, InventoryStatus status);
    List<Inventory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT i FROM Inventory i WHERE i.cell.id = :cellId AND i.status = 'PLACED'")
    List<Inventory> findActiveByCellId(@Param("cellId") Long cellId);

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "WHERE i.id = :id")
    Optional<Inventory> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "WHERE i.status = 'PLACED' " +
            "ORDER BY i.createdAt DESC")
    List<Inventory> findActiveInventoriesWithDetails();

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "ORDER BY i.createdAt DESC")
    List<Inventory> findAllWithDetails();

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "WHERE i.status <> 'ISSUED' " +
            "ORDER BY i.createdAt DESC")
    List<Inventory> findNotIssuedWithDetails();

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "WHERE LOWER(i.patient.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR i.patient.medicalCardNumber LIKE CONCAT('%', :query, '%') " +
            "ORDER BY i.createdAt DESC")
    List<Inventory> searchByCardOrFio(@Param("query") String query);

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "WHERE (i.patient.medicalCardNumber LIKE CONCAT('%', :query, '%') " +
            "OR LOWER(i.patient.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:status IS NULL OR i.status = :status)")
    List<Inventory> searchByQueryAndStatus(@Param("query") String query,
                                           @Param("status") InventoryStatus status);

    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.storage " +
            "LEFT JOIN FETCH i.cell " +
            "LEFT JOIN FETCH i.createdBy " +
            "WHERE i.patient.isDischarged = true AND i.status <> 'ISSUED' " +
            "ORDER BY i.createdAt DESC")
    List<Inventory> findActiveForDischargedPatients();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.createdAt BETWEEN :start AND :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.issuedAt BETWEEN :start AND :end")
    long countIssuedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (i.issued_at - i.created_at))) " +
            "FROM inventories i " +
            "WHERE i.status = 'ISSUED' " +
            "AND i.issued_at IS NOT NULL " +
            "AND i.created_at IS NOT NULL",
            nativeQuery = true)
    Double getAverageStorageTime();

    @Query("SELECT COUNT(c) FROM StorageCell c")
    long countTotalCells();

    @Query(value = "SELECT COUNT(DISTINCT i.cell_id) FROM inventories i " +
            "WHERE i.cell_id IS NOT NULL " +
            "AND i.created_at < CAST(:date AS DATE) + INTERVAL '1 day' " +
            "AND (i.issued_at IS NULL OR i.issued_at > CAST(:date AS DATE) + INTERVAL '1 day')",
            nativeQuery = true)
    Long countOccupiedCellsAtDate(@Param("date") LocalDate date);
}
