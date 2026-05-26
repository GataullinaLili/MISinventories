package com.itemstorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "storage_cells")
public class StorageCell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Storage storage;

    @Column(name = "is_occupied")
    private Boolean isOccupied = false;

    @Column(name = "qr_code_path", length = 500)
    private String qrCodePath;

    public StorageCell() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Boolean getIsOccupied() { return isOccupied; }
    public void setIsOccupied(Boolean isOccupied) { this.isOccupied = isOccupied; }

    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Storage storage;
        private Boolean isOccupied = false;
        private String qrCodePath;

        public Builder id(Long v) { this.id = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder storage(Storage v) { this.storage = v; return this; }
        public Builder isOccupied(Boolean v) { this.isOccupied = v; return this; }
        public Builder qrCodePath(String v) { this.qrCodePath = v; return this; }

        public StorageCell build() {
            StorageCell c = new StorageCell();
            c.setId(id);
            c.setName(name);
            c.setStorage(storage);
            c.setIsOccupied(isOccupied);
            c.setQrCodePath(qrCodePath);
            return c;
        }
    }
}