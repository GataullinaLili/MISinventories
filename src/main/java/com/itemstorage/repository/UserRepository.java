package com.itemstorage.repository;

import com.itemstorage.entity.User;
import com.itemstorage.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, Long id);

    // Новые методы
    List<User> findAllByOrderByCreatedAtDesc();
    long countByRoleAndActiveTrue(Role role);
}