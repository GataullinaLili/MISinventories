package com.itemstorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 500)
    private String description;

    @Column(name = "photo_path", length = 500)
    private String photoPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items"})
    private Inventory inventory;

    public Item() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Integer quantity = 1;
        private String description;
        private String photoPath;
        private Inventory inventory;

        public Builder id(Long v) { this.id = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder quantity(Integer v) { this.quantity = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder photoPath(String v) { this.photoPath = v; return this; }
        public Builder inventory(Inventory v) { this.inventory = v; return this; }

        public Item build() {
            Item item = new Item();
            item.setId(id);
            item.setName(name);
            item.setQuantity(quantity);
            item.setDescription(description);
            item.setPhotoPath(photoPath);
            item.setInventory(inventory);
            return item;
        }
    }
}