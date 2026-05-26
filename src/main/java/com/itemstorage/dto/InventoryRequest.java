package com.itemstorage.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание новой описи вещей")
public class InventoryRequest {

    @NotBlank(message = "Номер медицинской карты обязателен")
    @Size(min = 3, max = 50)
    @Schema(description = "Номер медицинской карты пациента",
            example = "ИБ-10001",
            required = true,
            minLength = 3,
            maxLength = 50)
    private String medicalCardNumber;

    @NotEmpty(message = "Должна быть хотя бы одна вещь")
    @Valid
    @ArraySchema(schema = @Schema(description = "Список вещей"),
            minItems = 1)
    private List<@Valid ItemRequest> items;
}