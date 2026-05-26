package com.itemstorage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:uploads/photos}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("=== ДИРЕКТОРИЯ ФОТО ===");
            log.info("Путь: {}", uploadPath);
            log.info("Существует: {}", Files.exists(uploadPath));
            log.info("======================");
        } catch (IOException e) {
            log.error("Не удалось создать директорию: {}", uploadPath, e);
        }
    }

    /**
     * Сохраняет файл и возвращает имя сохранённого файла.
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Попытка сохранить пустой файл");
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetPath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("=== ФАЙЛ СОХРАНЁН ===");
            log.info("Оригинал: {}", originalFilename);
            log.info("Сохранён как: {}", newFilename);
            log.info("Размер: {} bytes", file.getSize());
            log.info("Путь: {}", targetPath);
            log.info("====================");

            return newFilename;
        } catch (IOException e) {
            log.error("Ошибка сохранения файла: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Полный путь к файлу.
     */
    public Path getFilePath(String filename) {
        if (filename == null) return null;
        return uploadPath.resolve(filename).normalize();
    }

    public void deleteFile(String filename) {
        if (filename == null) return;
        try {
            Path filePath = uploadPath.resolve(filename);
            boolean deleted = Files.deleteIfExists(filePath);
            log.info("Удаление файла {}: {}", filename, deleted ? "успешно" : "не найден");
        } catch (IOException e) {
            log.warn("Не удалось удалить файл: {}", filename);
        }
    }
}