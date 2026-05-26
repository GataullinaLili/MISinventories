package com.itemstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о вещи в описи (ответ API)")
public class ItemResponse {

    @Schema(description = "Название вещи", example = "Куртка зимняя")
    private String name;

    @Schema(description = "Количество", example = "1")
    private Integer quantity;

    @Schema(description = "Описание вещи", example = "Синяя, размер L, с капюшоном")
    private String description;

    @Schema(description = "Наличие фотографии вещи", example = "true")
    private boolean hasPhoto;

    @Schema(description = "URL фотографии (если есть)", example = "/photos/abc123.jpg")
    private String photoUrl;
}