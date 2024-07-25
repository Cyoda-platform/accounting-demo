package com.example.accounting_demo.service;

import com.example.accounting_demo.auxiliary.*;
import com.example.accounting_demo.model.*;
import com.example.accounting_demo.processor.CyodaCalculationMemberClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    @Value("${cyoda.token}")
    private String token;

    @Value("${cyoda.host}")
    private String host;

    private final String MODEL_VERSION = "1";

    private final String ENTITY_CLASS_NAME = "com.cyoda.tdb.model.treenode.TreeNodeEntity";

    private final ObjectMapper om;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final JsonToEntityParser jsonToEntityParser;

    private final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(5000)
            .build();

    public EntityService(ObjectMapper om, JsonToEntityParser jsonToEntityParser) {
        this.om = om;
        this.jsonToEntityParser = jsonToEntityParser;
    }

    public <T extends BaseEntity> HttpResponse saveEntitySchema(List<T> entities) throws IOException {
        String model = ModelRegistry.getModelByClass(entities.get(0).getClass());

        String url = String.format("%s/api/treeNode/model/import/JSON/SAMPLE_DATA/%s/%s", host, model, MODEL_VERSION);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        httpPost.setHeader("Authorization", "Bearer " + token);
        httpPost.setHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        logger.info(om.writeValueAsString(httpPost.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return response;
        }
    }

    //    entities provided in order to define the model
    public <T extends BaseEntity> HttpResponse lockEntitySchema(List<T> entities) throws IOException {
        String model = ModelRegistry.getModelByClass(entities.get(0).getClass());

        String url = String.format("%s/api/treeNode/model/%s/%s/lock", host, model, MODEL_VERSION);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpPut.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return response;
        }
    }

    public <T extends BaseEntity> HttpResponse saveEntities(List<T> entities) throws IOException {
        String model = ModelRegistry.getModelByClass(entities.get(0).getClass());

        String url = String.format("%s/api/entity/new/JSON/TREE/%s/%s", host, model, MODEL_VERSION);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        StringEntity requestEntity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);

        logger.info("SAVE ENTITY REQUEST: " + om.writeValueAsString(httpPost.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return response;
        }
    }

    public HttpResponse deleteEntityByRootId(String modelName, String modelVersion, String rootId) throws IOException {
        String url = String.format(host + "/api/entity/TREE/%s/%s/%s", modelName, modelVersion, rootId);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(requestConfig);
        httpDelete.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpDelete.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.info(om.writeValueAsString(responseBody));
            return response;
        }
    }

    public HttpResponse deleteAllEntitiesByModel(String modelName, String modelVersion) throws IOException {
        String url = String.format(host + "/api/entity/TREE/%s/%s", modelName, modelVersion);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpDelete.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            return response;
        }
    }

    public <T extends BaseEntity> HttpResponse deleteAllEntitiesByModel(List<T> entities) throws IOException {
        String modelName = ModelRegistry.getModelByClass(entities.get(0).getClass());
        return deleteAllEntitiesByModel(modelName);
    }

    public HttpResponse deleteAllEntitiesByModel(String modelName) throws IOException {
        return deleteAllEntitiesByModel(modelName, MODEL_VERSION);
    }

    public HttpResponse deleteEntityModel(String modelName, String modelVersion) throws IOException {
        String url = String.format(host + "/api/treeNode/model/%s/%s", modelName, modelVersion);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpDelete.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            return response;
        }
    }

    public HttpResponse deleteEntityModel(String modelName) throws IOException {
        return deleteEntityModel(modelName, MODEL_VERSION);
    }

    public <T> String convertListToJson(List<T> entities) throws JsonProcessingException {
        return om.writeValueAsString(entities);
    }

    public String runSearchAndGetSnapshotId(String model, String version, SearchConditionRequest searchConditionRequest) throws IOException {
        String url = String.format("%s/api/treeNode/search/snapshot/%s/%s", host, model, version);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        String json = om.writeValueAsString(searchConditionRequest);
        httpPost.setEntity(new StringEntity(json));

        logger.info(om.writeValueAsString(httpPost.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity).trim();
            JsonNode jsonNode = om.readTree(responseBody);

            return jsonNode.asText();
        }
    }

    public Map<String, Object> getSnapshotStatus(String snapshotId) throws IOException {
        String url = String.format("%s/api/treeNode/search/snapshot/%s/status", host, snapshotId);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpGet.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            Map<String, Object> responseMap = om.readValue(responseString, new TypeReference<>() {
            });
            return responseMap;
        }
    }

    public String getSearchResultAsJson(String snapshotId) throws IOException {
        String url = String.format("%s/api/treeNode/search/snapshot/%s?pageSize=1000", host, snapshotId);
        return getRequest(url);
    }

    public String getAllEntitiesAsJsonWithoutIds(String model, String version) throws IOException {
        String url = String.format("%s/api/entity/TREE/%s/%s", host, model, version);
        return getRequest(url);
    }

    public String getRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpGet.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }
    }

//    temporarily used to fetch all entities with IDs by model, can be replaced by getAllEntitiesAsJsonWithoutIds if it provides IDs
    public <T extends BaseEntity> List<T> getAllEntitiesByModel(String model, String version) throws IOException, InterruptedException {
        var clazz = ModelRegistry.getClassByModel(model);
        var setFieldNames = FieldNameExtractor.getFieldNames(clazz);
        var iterator = setFieldNames.iterator();
        String firstFieldName = "";
        if (iterator.hasNext()) {
            firstFieldName = iterator.next();
        }

        var conditionRequest = new SearchConditionRequest();
        conditionRequest.setType("group");
        conditionRequest.setOperator("AND");
        conditionRequest.setConditions(List.of(
                new Condition("simple", "$." + firstFieldName, "NOT_EQUAL", "Impossible value")
        ));

        var snapshotId = runSearchAndGetSnapshotId(model, version, conditionRequest);

        int maxWaitTimeInMillis = 10000;
        int waitIntervalInMillis = 400;
        int waitedTime = 0;
        while ((!getSnapshotStatus(snapshotId).get("snapshotStatus").equals("SUCCESSFUL")) && waitedTime < maxWaitTimeInMillis) {
            TimeUnit.MILLISECONDS.sleep(waitIntervalInMillis);
            waitedTime += waitIntervalInMillis;
        }
        if (!getSnapshotStatus(snapshotId).get("snapshotStatus").equals("SUCCESSFUL")) {
            throw new IllegalStateException("Timeout: report is not ready");
        }

        var response = getSearchResultAsJson(snapshotId);
        return (List<T>) jsonToEntityParser.parseResponse(response, clazz);
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

        String fullColumnPath = "values@org#cyoda#entity#model#ValueMaps." + columnPath;
        String encodedColumnPath = URLEncoder.encode(fullColumnPath, StandardCharsets.UTF_8);
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

        String fullColumnPath = "values@org#cyoda#entity#model#ValueMaps." + columnPath;
        StringEntity entity = getStringEntity(fullColumnPath, newValue, entityId);
        httpPut.setEntity(entity);

        logger.info(om.writeValueAsString(httpPut.toString()));
        String requestBody = EntityUtils.toString(entity);
        Object json = om.readValue(requestBody, Object.class);
        logger.info(om.writerWithDefaultPrettyPrinter().writeValueAsString(json));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return response;
        }
    }

    private StringEntity getStringEntity(String columnPath, JsonNode value, String entityId) throws UnsupportedEncodingException {
        String requestBody = String.format("""
                {
                  "entityClass": "%s",
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
                }""", ENTITY_CLASS_NAME, entityId, columnPath, value);

        return new StringEntity(requestBody);
    }
}

