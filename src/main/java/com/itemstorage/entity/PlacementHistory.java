package com.itemstorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "placement_history")
public class PlacementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "createdBy"})
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StorageCell cell;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 150)
    private String performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(name = "previous_cell", length = 50)
    private String previousCell;

    @Column(name = "new_cell", length = 50)
    private String newCell;

    public PlacementHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public StorageCell getCell() { return cell; }
    public void setCell(StorageCell cell) { this.cell = cell; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getPreviousCell() { return previousCell; }
    public void setPreviousCell(String previousCell) { this.previousCell = previousCell; }

    public String getNewCell() { return newCell; }
    public void setNewCell(String newCell) { this.newCell = newCell; }
}