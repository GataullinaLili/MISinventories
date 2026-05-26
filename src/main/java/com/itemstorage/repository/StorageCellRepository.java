package com.itemstorage.repository;

import com.itemstorage.entity.StorageCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StorageCellRepository extends JpaRepository<StorageCell, Long> {

    @Query("SELECT c FROM StorageCell c JOIN FETCH c.storage WHERE c.storage.id = :storageId")
    List<StorageCell> findByStorageIdWithFetch(@Param("storageId") Long storageId);

    @Query("SELECT c FROM StorageCell c JOIN FETCH c.storage")
    List<StorageCell> findAllWithStorage();

    @Query("SELECT c FROM StorageCell c WHERE c.storage.id = :storageId AND c.isOccupied = false")
    List<StorageCell> findFreeByStorageId(@Param("storageId") Long storageId);
}