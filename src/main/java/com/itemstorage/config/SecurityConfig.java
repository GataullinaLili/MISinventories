package com.itemstorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import javax.sql.DataSource;

/**
 * Конфигурация безопасности приложения.
 * Настраивает аутентификацию, авторизацию, CSRF защиту и Remember Me.
 *
 * @author YourName
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Value("${app.security.remember-me.key:uniqueAndSecretKey}")
    private String rememberMeKey;

    /**
     * Основной фильтр безопасности.
     * Настраивает все аспекты безопасности приложения.
     *
     * @param http HttpSecurity для конфигурации
     * @param tokenRepository репозиторий для Remember Me токенов
     * @return сконфигурированный SecurityFilterChain
     * @throws Exception если конфигурация невалидна
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   PersistentTokenRepository tokenRepository) throws Exception {
        http
                // Защита HTTP-заголовков
                .headers(headers -> headers
                        // XSS защита
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        // Content Security Policy
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self' 'unsafe-inline' https://unpkg.com; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data: blob:; " +
                                                "frame-src 'none'"))
                        // Запрет встраивания в iframe
                        .frameOptions(frame -> frame.deny())
                )

                // CSRF защита
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/qrcode")
                )

                // Управление сессиями
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired"))

                // Remember Me (запомнить меня)
                .rememberMe(remember -> remember
                        .tokenRepository(tokenRepository)
                        .key(rememberMeKey)
                        .tokenValiditySeconds(86400))  // 24 часа

                // Авторизация запросов
                .authorizeHttpRequests(auth -> auth
                        // Публичные статические ресурсы
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**",
                                "/photos/**", "/sw.js", "/offline.html",
                                "/webjars/**", "/actuator/health"
                        ).permitAll()

                        // Логин и Swagger документация
                        .requestMatchers(
                                "/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // QR-код доступен всем
                        .requestMatchers("/api/qrcode").permitAll()

                        // Администратор
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Кладовщик
                        .requestMatchers("/storekeeper/**").hasAnyRole("STOREKEEPER", "ADMIN")

                        // Аналитика
                        .requestMatchers("/analyst/**").hasAnyRole("ANALYST", "ADMIN", "STOREKEEPER")

                        // Приёмщик
                        .requestMatchers("/receptionist/**").hasAnyRole("RECEPTIONIST", "STOREKEEPER", "ADMIN")

                        // Выписка пациентов
                        .requestMatchers("/discharge/**").hasRole("ADMIN")

                        // API endpoints
                        .requestMatchers("/api/**").authenticated()

                        // Пациенты
                        .requestMatchers("/patients/**").hasAnyRole("ADMIN")

                        // Поиск
                        .requestMatchers("/search", "/search/**").authenticated()

                        // Описи
                        .requestMatchers("/inventories", "/inventories/**").authenticated()

                        // Словарь
                        .requestMatchers("/dictionary").hasAnyRole("ADMIN", "STOREKEEPER", "RECEPTIONIST")

                        // Инструкция
                        .requestMatchers("/instructions/**").hasAnyRole("ADMIN", "STOREKEEPER", "RECEPTIONIST")

                        .requestMatchers("/exit").authenticated()

                        // Все остальные запросы требуют авторизации
                        .anyRequest().authenticated()
                )

                // Форма логина
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/search", true)
                        .failureUrl("/login?error")
                        .permitAll())

                // Выход из системы
                .logout(logout -> logout
                        .logoutUrl("/exit")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll());

        return http.build();
    }

    /**
     * Кодировщик паролей BCrypt с силой хеширования 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Репозиторий для хранения Remember Me токенов в базе данных.
     * Требует таблицу persistent_logins в БД.
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // Раскомментировать при ПЕРВОМ запуске для создания таблицы:
        // tokenRepository.setCreateTableOnStartup(true);
        return tokenRepository;
    }
}