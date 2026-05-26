package com.itemstorage.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medical_card_number", nullable = false, unique = true, length = 50)
    private String medicalCardNumber;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_discharged")
    private Boolean isDischarged = false;

    @Column(name = "discharged_at")
    private LocalDateTime dischargedAt;

    public Patient() {}

    public Patient(Long id, String medicalCardNumber, String fullName, LocalDate birthDate,
                   LocalDateTime createdAt, Boolean isDischarged, LocalDateTime dischargedAt) {
        this.id = id;
        this.medicalCardNumber = medicalCardNumber;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.isDischarged = isDischarged;
        this.dischargedAt = dischargedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicalCardNumber() { return medicalCardNumber; }
    public void setMedicalCardNumber(String medicalCardNumber) { this.medicalCardNumber = medicalCardNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsDischarged() { return isDischarged; }
    public void setIsDischarged(Boolean isDischarged) { this.isDischarged = isDischarged; }

    public LocalDateTime getDischargedAt() { return dischargedAt; }
    public void setDischargedAt(LocalDateTime dischargedAt) { this.dischargedAt = dischargedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String medicalCardNumber;
        private String fullName;
        private LocalDate birthDate;
        private LocalDateTime createdAt = LocalDateTime.now();
        private Boolean isDischarged = false;
        private LocalDateTime dischargedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder medicalCardNumber(String v) { this.medicalCardNumber = v; return this; }
        public Builder fullName(String v) { this.fullName = v; return this; }
        public Builder birthDate(LocalDate v) { this.birthDate = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder isDischarged(Boolean v) { this.isDischarged = v; return this; }
        public Builder dischargedAt(LocalDateTime v) { this.dischargedAt = v; return this; }

        public Patient build() {
            return new Patient(id, medicalCardNumber, fullName, birthDate, createdAt, isDischarged, dischargedAt);
        }
    }
}