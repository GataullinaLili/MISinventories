package com.itemstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Вещь для добавления в опись")
public class ItemRequest {

    @NotBlank(message = "Название вещи обязательно")
    @Size(min = 2, max = 200)
    @Schema(description = "Название вещи",
            example = "Куртка зимняя",
            required = true,
            minLength = 2,
            maxLength = 200)
    private String name;

    @Min(value = 1)
    @Schema(description = "Количество единиц",
            example = "1",
            minimum = "1",
            maximum = "100",
            defaultValue = "1")
    private Integer quantity = 1;

    @Size(max = 500)
    @Schema(description = "Описание (цвет, размер, особенности)",
            example = "Синяя, размер L",
            maxLength = 500)
    private String description;
}