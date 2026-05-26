package com.itemstorage.repository;

import com.itemstorage.entity.Storage;
import com.itemstorage.enums.StorageType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByStorageType(StorageType storageType);
}