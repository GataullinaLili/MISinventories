package com.itemstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об описи (ответ API)")
public class InventoryResponse {

    @Schema(description = "Уникальный идентификатор описи", example = "1001")
    private Long id;

    @Schema(description = "Номер медицинской карты пациента", example = "ИБ-10001")
    private String medicalCardNumber;

    @Schema(description = "ФИО пациента", example = "Иванов Иван Иванович")
    private String patientName;

    @Schema(description = "Текущий статус описи",
            example = "PLACED",
            allowableValues = {"CREATED", "PLACED", "MOVED", "ISSUED"})
    private String status;

    @Schema(description = "Название склада размещения", example = "Склад долговременного хранения №1")
    private String storageName;

    @Schema(description = "Название ячейки", example = "А-01")
    private String cellName;

    @Schema(description = "ФИО создателя описи", example = "Петрова Анна Петровна")
    private String createdBy;

    @Schema(description = "Дата и время создания описи")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время выдачи (если выдана)")
    private LocalDateTime issuedAt;

    @Schema(description = "Список вещей в описи")
    private List<ItemResponse> items;
}