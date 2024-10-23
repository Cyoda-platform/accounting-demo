package com.example.accounting_demo.common.ai;

import com.example.accounting_demo.common.auth.Authentication;
import com.example.accounting_demo.common.ingestion.RequestDto;
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
public class AIAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);

    @Value("${cyoda.host}")
    private String host;

    private final String token;

    public final RestTemplate restTemplate;

    public AIAssistantService(Authentication authentication, RestTemplate restTemplate) {
        this.token = authentication.getToken();
        this.restTemplate = restTemplate;
    }

    public String chat(String id, String question) {
        try {
            // Create the request DTO
            AIRequestDto requestDTO = new AIRequestDto();
            requestDTO.setId(id);
            requestDTO.setQuestion(question);

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
                    host + "/ai/api/v1/trino/chat?chat_id="+requestDTO.getId(),
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

}
