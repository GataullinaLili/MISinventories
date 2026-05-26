package com.itemstorage.enums;

public enum InventoryStatus {
    CREATED("Создана"),
    PLACED("Размещена"),
    MOVED("Перемещена"),
    ISSUED("Выдана");

    private final String displayName;

    InventoryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
