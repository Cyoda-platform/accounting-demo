package com.example.accounting_demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class EntityService {

    @Value("${my.token}")
    private String token;

    private String entityClass = "com.cyoda.tdb.model.treenode.TreeNodeEntity";

    @Autowired
    private ObjectMapper om;
    private final HttpClient httpClient = HttpClients.createDefault();

    public HttpResponse launchTransition(UUID id, String transition) throws IOException {

        String entityId = id.toString();
        String url = String.format("http://localhost:8082/api/platform-api/entity/transition?entityId=%s&%s&transitionName=%s", entityId, entityClass, transition);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        System.out.println(httpPut);

        return httpClient.execute(httpPut);
    }

    public String getCurrentState(UUID id) throws IOException {

        String entityId = id.toString();
        String url = String.format("http://localhost:8082/api/platform-api/entity-info/fetch/lazy?%s&entityId=%s&columnPath=state", entityClass, entityId);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + token);

        System.out.println(httpGet);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        JsonNode jsonNode = om.readTree(responseBody);

        return jsonNode.get(0).get("value").asText();
    }

    public String getValue(UUID id, String columnPath) throws IOException {

        String encodedColumnPath = URLEncoder.encode(columnPath, StandardCharsets.UTF_8);
        String entityId = id.toString();
        String url = String.format("http://localhost:8082/api/platform-api/entity-info/fetch/lazy?%s&entityId=%s&columnPath=%s", entityClass, entityId, encodedColumnPath);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + token);

        System.out.println(httpGet);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        JsonNode jsonNode = om.readTree(responseBody);

        System.out.println(responseBody);

        return jsonNode.get(0).get("value").asText();
    }
//TODO edit this method to take a list of columnPath+newValue
    public HttpResponse updateValue(UUID id, String columnPath, JsonNode newValue) throws IOException {
        String entityId = id.toString();
        String url = "http://localhost:8082/api/platform-api/entity";
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);
        httpPut.setHeader("Content-Type", "application/json");

        StringEntity entity = getStringEntity(columnPath, newValue, entityId);
        httpPut.setEntity(entity);

        System.out.println(httpPut);
        String requestBody = EntityUtils.toString(entity);
        System.out.println(om.readTree(requestBody));

        return httpClient.execute(httpPut);
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

        StringEntity entity = new StringEntity(requestBody);
        return entity;
    }
}

