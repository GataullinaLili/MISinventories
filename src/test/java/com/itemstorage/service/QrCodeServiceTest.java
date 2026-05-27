package com.itemstorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    @DisplayName("Должен сгенерировать QR-код для числа")
    void testGenerateQrCodeBase64_WithNumber() {
        String result = qrCodeService.generateQrCodeBase64("12345");

        assertThat(result).isNotEmpty();
        assertThat(result).startsWith("data:image/png;base64,");
    }

    @Test
    @DisplayName("Должен извлечь номер из формата INV-12345")
    void testGenerateQrCodeBase64_WithInvPrefix() {
        String result = qrCodeService.generateQrCodeBase64("INV-12345");

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Должен извлечь номер из формата Опись №12345")
    void testGenerateQrCodeBase64_WithRussianPrefix() {
        String result = qrCodeService.generateQrCodeBase64("Опись №12345");

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Должен вернуть пустую строку при некорректных данных")
    void testGenerateQrCodeBase64_InvalidData() {
        String result = qrCodeService.generateQrCodeBase64("NO_NUMBER");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть пустую строку при пустых данных")
    void testGenerateQrCodeBase64_EmptyData() {
        String result = qrCodeService.generateQrCodeBase64("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть пустую строку при null")
    void testGenerateQrCodeBase64_NullData() {
        String result = qrCodeService.generateQrCodeBase64(null);

        assertThat(result).isEmpty();
    }
}
