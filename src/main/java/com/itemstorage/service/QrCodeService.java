package com.itemstorage.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeService.class);

    public String generateQrCodeBase64(String data) {
        try {
            String cleanData = extractInventoryNumber(data);

            if (cleanData.isEmpty()) {
                log.warn("Не удалось извлечь номер описи из данных: {}", data);
                return "";
            }

            log.debug("Генерация QR-кода для данных: {} -> номер описи: {}", data, cleanData);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(cleanData, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            byte[] bytes = baos.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("Ошибка генерации QR-кода для данных: {}", data, e);
            return "";
        }
    }

    private String extractInventoryNumber(String data) {
        if (data == null || data.trim().isEmpty()) {
            return "";
        }

        String cleaned = data.trim();

        if (cleaned.matches("\\d+")) {
            return cleaned;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:INV-)?(\\d+)(?:-|\\s|$)");
        java.util.regex.Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            return matcher.group(1);
        }

        java.util.regex.Pattern firstNumberPattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher firstMatcher = firstNumberPattern.matcher(cleaned);
        if (firstMatcher.find()) {
            return firstMatcher.group();
        }

        return "";
    }
}