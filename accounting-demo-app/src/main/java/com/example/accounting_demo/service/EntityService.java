package com.example.accounting_demo.service;

import com.example.accounting_demo.processor.CyodaCalculationMemberClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    @Value("${cyoda.token}")
    private String token;

    @Value("${cyoda.host}")
    private String host;

    private final String ENTITY_CLASS_NAME = "com.cyoda.tdb.model.treenode.TreeNodeEntity";

    private final ObjectMapper om;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(5000)
            .build();

    public EntityService(ObjectMapper om) {
        this.om = om;
    }

    public HttpResponse launchTransition(UUID id, String transition) throws IOException {

        String entityId = id.toString();
        String url = String.format("%s/api/platform-api/entity/transition?entityId=%s&entityClass=%s&transitionName=%s", host, entityId, ENTITY_CLASS_NAME, transition);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        httpPut.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpPut.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return response;
        }
    }

    public List<String> getListTransitions(UUID id) throws IOException {
        String entityId = id.toString();
        String url = String.format("%s/api/platform-api/entity/fetch/transitions?entityId=%s&entityClass=%s", host, entityId, ENTITY_CLASS_NAME);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpGet.toString()));

        List<String> stringList = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JsonNode jsonNode = om.readTree(responseBody);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    stringList.add(node.asText());
                }
            }
        }

        return stringList;
    }


    public String getCurrentState(UUID id) throws IOException {

        String entityId = id.toString();
        String url = String.format("%s/api/platform-api/entity-info/fetch/lazy?entityClass=%s&entityId=%s&columnPath=state", host, ENTITY_CLASS_NAME, entityId);
        return getUrlString(url);
    }

    public String getValue(UUID id, String columnPath) throws IOException {

        String encodedColumnPath = URLEncoder.encode(columnPath, StandardCharsets.UTF_8);
        String entityId = id.toString();
        String url = String.format("%s/api/platform-api/entity-info/fetch/lazy?entityClass=%s&entityId=%s&columnPath=%s", host, ENTITY_CLASS_NAME, entityId, encodedColumnPath);
        return getUrlString(url);
    }

    private String getUrlString(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpGet.toString()));

        JsonNode jsonNode;

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            jsonNode = om.readTree(responseBody);
        }

        return jsonNode.get(0).get("value").asText();
    }

    //TODO edit this method to take a list of columnPath+newValue
    public HttpResponse updateValue(UUID id, String columnPath, JsonNode newValue) throws IOException {
        String entityId = id.toString();
        String url = host + "/api/platform-api/entity";
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);
        httpPut.setHeader("Content-Type", "application/json");

        StringEntity entity = getStringEntity(columnPath, newValue, entityId);
        httpPut.setEntity(entity);

        logger.info(om.writeValueAsString(httpPut.toString()));
        String requestBody = EntityUtils.toString(entity);
        logger.info(om.writeValueAsString(requestBody.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return response;
        }
    }

    //TODO add a method to delete entities, taking a list of ids
    private static StringEntity getStringEntity(String columnPath, JsonNode value, String entityId) throws UnsupportedEncodingException {
        String requestBody = String.format("""
                {
                  "entityClass": "com.cyoda.tdb.model.treenode.TreeNodeEntity",
                  "entityId": "%s",
                  "transition": "UPDATE",
                  "transactional": true,
                  "async": false,
                  "values": [
                    {
                      "columnPath": "%s",
                      "value": %s
                    }
                  ]
                }""", entityId, columnPath, value);

        return new StringEntity(requestBody);
    }
}

