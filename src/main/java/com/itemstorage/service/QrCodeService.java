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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeService.class);

    private static final int QR_CODE_WIDTH = 200;
    private static final int QR_CODE_HEIGHT = 200;

    public String generateQrCodeBase64(String data) {
        try {
            String cleanData = extractInventoryNumber(data);

            if (cleanData.isEmpty()) {
                log.warn("Не удалось извлечь номер описи из данных: {}", data);
                return "";
            }

            log.debug("Генерация QR-кода для данных: {} -> номер описи: {}", data, cleanData);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(cleanData, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);

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

        if (cleaned.toLowerCase().contains("http") || cleaned.toLowerCase().contains("localhost") || cleaned.toLowerCase().contains("www")) {
            Pattern urlPattern = Pattern.compile("/(\\d+)(?:/|$|\\?|#)");
            Matcher urlMatcher = urlPattern.matcher(cleaned);
            if (urlMatcher.find()) {
                return urlMatcher.group(1);
            }
            // Пробуем найти любую последовательность цифр в URL
            Pattern anyInUrl = Pattern.compile("\\d+");
            Matcher anyMatcher = anyInUrl.matcher(cleaned);
            if (anyMatcher.find()) {
                return anyMatcher.group();
            }
            return "";
        }

        Pattern pattern = Pattern.compile("(?:INV-|inv-|№)?(\\d+)");
        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1);
        }

        Pattern firstNumberPattern = Pattern.compile("\\d+");
        Matcher firstMatcher = firstNumberPattern.matcher(cleaned);
        if (firstMatcher.find()) {
            return firstMatcher.group();
        }

        return "";
    }
}