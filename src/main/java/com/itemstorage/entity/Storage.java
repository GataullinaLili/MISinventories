package com.itemstorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itemstorage.enums.StorageType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "storages")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;

    @OneToMany(mappedBy = "storage", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<StorageCell> cells = new ArrayList<>();

    public Storage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StorageType getStorageType() { return storageType; }
    public void setStorageType(StorageType storageType) { this.storageType = storageType; }

    public List<StorageCell> getCells() { return cells; }
    public void setCells(List<StorageCell> cells) { this.cells = cells; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private StorageType storageType;
        private List<StorageCell> cells = new ArrayList<>();

        public Builder id(Long v) { this.id = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder storageType(StorageType v) { this.storageType = v; return this; }
        public Builder cells(List<StorageCell> v) { this.cells = v; return this; }

        public Storage build() {
            Storage s = new Storage();
            s.setId(id);
            s.setName(name);
            s.setStorageType(storageType);
            s.setCells(cells);
            return s;
        }
    }
}