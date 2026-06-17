package com.itemstorage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("АРМ.Вещи - API документация")
                        .version("1.0.0")
                        .description("""
                    Система учёта личных вещей пациентов.
                    
                    ## Основные функции:
                    - Управление описями вещей
                    - Размещение на складах
                    - Выдача вещей пациентам
                    - Интеграция с МИС
                    - Печать QR-кодов
                    
                    ## Роли пользователей:
                    - **ADMIN** - администратор системы
                    - **STOREKEEPER** - сотрудник склада
                    - **RECEPTIONIST** - сотрудник приемного отделения
                    - **ANALYST** - аналитик
                    """)
                        .contact(new Contact()
                                .name("Ваше Имя")
                                .email("your.email@example.com")
                                .url("https://github.com/yourusername"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Локальный сервер (HTTP) — РЕКОМЕНДУЕТСЯ")
                ))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic аутентификация (логин:пароль)"))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT токен (если используется)")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}