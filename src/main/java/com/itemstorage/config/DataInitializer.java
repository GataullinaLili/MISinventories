package com.itemstorage.config;

import com.itemstorage.entity.Storage;
import com.itemstorage.entity.StorageCell;
import com.itemstorage.entity.User;
import com.itemstorage.enums.Role;
import com.itemstorage.enums.StorageType;
import com.itemstorage.repository.StorageCellRepository;
import com.itemstorage.repository.StorageRepository;
import com.itemstorage.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final StorageCellRepository storageCellRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StorageRepository storageRepository,
                           StorageCellRepository storageCellRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storageRepository = storageRepository;
        this.storageCellRepository = storageCellRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByLogin("ADMIN")) {
            User admin = User.builder()
                    .login("ADMIN")
                    .passwordHash(passwordEncoder.encode("Admin12345!"))
                    .fullName("Администратор Системы")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            System.out.println("=== СОЗДАН админ" +
                    ": логин=ADMIN ===");
        }

        if (!userRepository.existsByLogin("SKLAD")) {
            User storekeeper = User.builder()
                    .login("SKLAD")
                    .passwordHash(passwordEncoder.encode("Sklad123!"))
                    .fullName("Петрова Анна Петровна")
                    .role(Role.STOREKEEPER)
                    .active(true)
                    .build();
            userRepository.save(storekeeper);
            System.out.println("=== СОЗДАН сотрудник склада: логин=SKLAD===");
        }

        if (!userRepository.existsByLogin("PRIEM")) {
            User receptionist = User.builder()
                    .login("PRIEM")
                    .passwordHash(passwordEncoder.encode("Qwerty123!"))
                    .fullName("Сидорова Анна Сергеевна")
                    .role(Role.RECEPTIONIST)
                    .active(true)
                    .build();
            userRepository.save(receptionist);
            System.out.println("=== СОЗДАН сотрудник приемного отделения: логин=PRIEM ===");
        }
        if (!userRepository.existsByLogin("ANALYST")) {
            User analyst = User.builder()
                    .login("ANALYST")
                    .passwordHash(passwordEncoder.encode("Analyst123!"))
                    .fullName("Иванова Мария Сергеевна")
                    .role(Role.ANALYST)
                    .active(true)
                    .build();
            userRepository.save(analyst);
            System.out.println("=== СОЗДАН аналитик: логин=ANALYST ===");
        }


        if (storageRepository.count() == 0) {
            Storage reception = Storage.builder()
                    .name("Склад приёмного отделения")
                    .storageType(StorageType.RECEPTION)
                    .build();
            storageRepository.save(reception);

            Storage longTerm = Storage.builder()
                    .name("Склад долговременного хранения №1")
                    .storageType(StorageType.LONG_TERM)
                    .build();
            storageRepository.save(longTerm);

            System.out.println("=== СОЗДАНЫ СКЛАДЫ ===");

            for (int i = 1; i <= 5; i++) {
                StorageCell cell = StorageCell.builder()
                        .name("Ячейка А-0" + i)
                        .storage(longTerm)
                        .isOccupied(false)
                        .build();
                storageCellRepository.save(cell);
            }

            for (int i = 1; i <= 3; i++) {
                StorageCell cell = StorageCell.builder()
                        .name("Ячейка Б-0" + i)
                        .storage(longTerm)
                        .isOccupied(false)
                        .build();
                storageCellRepository.save(cell);
            }

            System.out.println("=== СОЗДАНЫ ЯЧЕЙКИ ===");
        }

        System.out.println("=== ИНИЦИАЛИЗАЦИЯ ДАННЫХ ЗАВЕРШЕНА ===");
    }
}