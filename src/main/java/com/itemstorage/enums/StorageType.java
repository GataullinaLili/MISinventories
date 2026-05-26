package com.itemstorage.enums;

public enum StorageType {
    RECEPTION("Склад приёмного отделения"),
    LONG_TERM("Склад долговременного хранения");

    private final String displayName;

    StorageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
