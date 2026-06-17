package com.itemstorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itemstorage.enums.InventoryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventories")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Storage storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StorageCell cell;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryStatus status = InventoryStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "placed_by_name", length = 150)
    private String placedByName;

    @Column(name = "moved_at")
    private LocalDateTime movedAt;

    @Column(name = "moved_by_name", length = 150)
    private String movedByName;

    @Column(name = "previous_cell", length = 50)
    private String previousCell;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "issued_by", length = 150)
    private String issuedBy;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();

    public Inventory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public StorageCell getCell() { return cell; }
    public void setCell(StorageCell cell) { this.cell = cell; }

    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public String getPlacedByName() { return placedByName; }
    public void setPlacedByName(String placedByName) { this.placedByName = placedByName; }

    public LocalDateTime getMovedAt() { return movedAt; }
    public void setMovedAt(LocalDateTime movedAt) { this.movedAt = movedAt; }

    public String getMovedByName() { return movedByName; }
    public void setMovedByName(String movedByName) { this.movedByName = movedByName; }

    public String getPreviousCell() { return previousCell; }
    public void setPreviousCell(String previousCell) { this.previousCell = previousCell; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Patient patient;
        private Storage storage;
        private StorageCell cell;
        private InventoryStatus status = InventoryStatus.CREATED;
        private User createdBy;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime placedAt;
        private String placedByName;
        private LocalDateTime movedAt;
        private String movedByName;
        private String previousCell;
        private LocalDateTime issuedAt;
        private String issuedBy;
        private List<Item> items = new ArrayList<>();

        public Builder id(Long v) { this.id = v; return this; }
        public Builder patient(Patient v) { this.patient = v; return this; }
        public Builder storage(Storage v) { this.storage = v; return this; }
        public Builder cell(StorageCell v) { this.cell = v; return this; }
        public Builder status(InventoryStatus v) { this.status = v; return this; }
        public Builder createdBy(User v) { this.createdBy = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder placedAt(LocalDateTime v) { this.placedAt = v; return this; }
        public Builder placedByName(String v) { this.placedByName = v; return this; }
        public Builder movedAt(LocalDateTime v) { this.movedAt = v; return this; }
        public Builder movedByName(String v) { this.movedByName = v; return this; }
        public Builder previousCell(String v) { this.previousCell = v; return this; }
        public Builder issuedAt(LocalDateTime v) { this.issuedAt = v; return this; }
        public Builder issuedBy(String v) { this.issuedBy = v; return this; }
        public Builder items(List<Item> v) { this.items = v; return this; }

        public Inventory build() {
            Inventory inv = new Inventory();
            inv.setId(id);
            inv.setPatient(patient);
            inv.setStorage(storage);
            inv.setCell(cell);
            inv.setStatus(status);
            inv.setCreatedBy(createdBy);
            inv.setCreatedAt(createdAt);
            inv.setPlacedAt(placedAt);
            inv.setPlacedByName(placedByName);
            inv.setMovedAt(movedAt);
            inv.setMovedByName(movedByName);
            inv.setPreviousCell(previousCell);
            inv.setIssuedAt(issuedAt);
            inv.setIssuedBy(issuedBy);
            inv.setItems(items);
            return inv;
        }
    }
}