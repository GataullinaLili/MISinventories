package com.itemstorage.service;

import com.itemstorage.dto.PatientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MisIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(MisIntegrationService.class);

    @Value("${mis.api.url:http://localhost:8081/mis/api}")
    private String misApiUrl;

    private final RestTemplate restTemplate;

    public MisIntegrationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Получение всех пациентов из МИС.
     */
    public List<PatientDTO> getAllPatients() {
        try {
            String url = misApiUrl + "/patients?size=1000";
            log.info("Запрос всех пациентов из МИС: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.get("patients") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> patientsData = (List<Map<String, Object>>) body.get("patients");

                List<PatientDTO> result = new ArrayList<>();
                for (Map<String, Object> data : patientsData) {
                    PatientDTO dto = convertToDTO(data);
                    result.add(dto);
                }

                log.info("Загружено {} пациентов из МИС", result.size());
                return result;
            } else {
                log.warn("МИС вернул пустой ответ");
            }
        } catch (Exception e) {
            log.error("Ошибка загрузки пациентов из МИС: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * Поиск пациентов в МИС по запросу (ФИО, номер истории, ID МИС).
     */
    public List<PatientDTO> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = misApiUrl + "/patient/search?q=" + encodedQuery;
            log.info("Поиск в МИС: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.get("patients") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> patientsData = (List<Map<String, Object>>) body.get("patients");

                List<PatientDTO> result = new ArrayList<>();
                for (Map<String, Object> data : patientsData) {
                    PatientDTO dto = convertToDTO(data);
                    result.add(dto);
                }

                log.info("Найдено {} пациентов по запросу '{}'", result.size(), query);
                return result;
            }
        } catch (Exception e) {
            log.error("Ошибка поиска в МИС: {}", e.getMessage());
        }
        return List.of();
    }

    public PatientDTO getPatientByCard(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        try {
            String url = misApiUrl + "/patient/by-card/" + cardNumber.trim();
            log.info("Запрос пациента из МИС по карте: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.get("misId") != null) {
                PatientDTO dto = convertToDTO(body);
                log.info("Пациент получен из МИС: {} ({})", dto.getFullName(), dto.getMedicalCardNumber());
                return dto;
            } else {
                log.warn("Пациент с картой {} не найден в МИС", cardNumber);
            }
        } catch (Exception e) {
            log.error("Ошибка получения пациента из МИС по карте {}: {}", cardNumber, e.getMessage());
        }
        return null;
    }

    private PatientDTO convertToDTO(Map<String, Object> data) {
        PatientDTO dto = new PatientDTO();
        dto.setMisId(getString(data, "misId"));
        dto.setFullName(getString(data, "fullName"));
        dto.setBirthDate(getString(data, "birthDate"));
        dto.setMedicalCardNumber(getString(data, "medicalCardNumber"));
        dto.setDiagnosis(getString(data, "diagnosis"));
        dto.setDepartment(getString(data, "department"));
        dto.setAdmissionDate(getString(data, "admissionDate"));

        // Статус выписки — проверяем оба варианта
        Object dischargedObj = data.get("discharged");
        if (dischargedObj instanceof Boolean) {
            dto.setDischarged((Boolean) dischargedObj);
        } else if (dischargedObj instanceof String) {
            dto.setDischarged("true".equalsIgnoreCase((String) dischargedObj));
        } else {
            dto.setDischarged(false);
        }

        dto.setDischargeDate(getString(data, "dischargeDate"));

        return dto;
    }

    /**
     * Безопасное получение строки из Map.
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}