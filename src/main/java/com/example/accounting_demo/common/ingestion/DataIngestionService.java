package com.example.accounting_demo.common.ingestion;

import com.example.accounting_demo.common.auth.Authentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.Map;

@Component
public class DataIngestionService {
    private static final Logger logger = LoggerFactory.getLogger(DataIngestionService.class);

    @Value("${cyoda.host}")
    private String host;

    private final String token;

    public final RestTemplate restTemplate;

    public DataIngestionService(Authentication authentication, RestTemplate restTemplate) {
        this.token = authentication.getToken();
        this.restTemplate = restTemplate;
    }

    public String ingestData(String datasourceId, String endpoint, Map<String, String> parameters) {
        try {
            // Create the request DTO
            RequestDto.DataSourceOperation operation = new RequestDto.DataSourceOperation(endpoint, parameters);
            RequestDto requestDTO = new RequestDto(Collections.singletonList(operation), datasourceId);

            // Convert DTO to JSON using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInputString = objectMapper.writeValueAsString(requestDTO);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + token);

            // Create the request entity
            HttpEntity<String> entity = new HttpEntity<>(jsonInputString, headers);

            // Make the POST request
            ResponseEntity<String> response = restTemplate.exchange(
                    host + "/api/data-source/request/request",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Log and return the response
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.info("Request failed! Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error during data ingestion: " + e.getMessage());
        }
        return null;
    }

    public String getDataSourceResult(String id) {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + token);

            // Create HttpEntity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the GET request
            ResponseEntity<String> response = restTemplate.exchange(
                    host + "/api/data-source/request/result/" + id,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            // Log and return the response
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                logger.info("Request failed! Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error during GET request: " + e.getMessage());
        }
        return null;
    }
}
