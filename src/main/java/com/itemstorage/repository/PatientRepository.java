package com.itemstorage.repository;

import com.itemstorage.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByMedicalCardNumber(String medicalCardNumber);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Patient> findByFullNameContainingIgnoreCase(@Param("query") String query);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :fio, '%')) " +
            "AND p.birthDate = :birthDate")
    List<Patient> findByFullNameAndBirthDate(@Param("fio") String fio,
                                             @Param("birthDate") LocalDate birthDate);

    @Query("SELECT p FROM Patient p WHERE p.medicalCardNumber LIKE CONCAT('%', :cardNumber, '%')")
    List<Patient> findByMedicalCardNumberContaining(@Param("cardNumber") String cardNumber);

    @Query("SELECT DISTINCT p FROM Patient p JOIN Inventory i ON i.patient = p " +
            "WHERE p.isDischarged = true AND i.status <> 'ISSUED'")
    List<Patient> findDischargedWithActiveInventories();

    List<Patient> findByIsDischargedTrue();

    long countByIsDischargedTrue();  // <-- ДОБАВЛЕНО
}