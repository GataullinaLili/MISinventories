package com.itemstorage.controller;

import com.itemstorage.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "QR Code API", description = "Генерация QR-кодов")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @Operation(summary = "Сгенерировать QR-код", description = "Генерирует QR-код в формате Base64 для печати")
    @GetMapping("/qrcode")
    public String generateQr(
            @Parameter(description = "Данные для QR-кода (номер описи)", example = "1001", required = true)
            @RequestParam String data) {
        log.debug("Запрос на генерацию QR с данными: {}", data);
        return qrCodeService.generateQrCodeBase64(data);
    }
}