package com.example.accounting_demo.service;

import com.example.accounting_demo.processor.CyodaCalculationMemberClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class EntityPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    private final String MODEL_VERSION = "1";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(5000)
            .build();

    @Value("${cyoda.host}")
    private String host;

    @Value("${cyoda.token}")
    private String token;

    final ObjectMapper om;
    final EntityIdLists entityIdLists;

    public EntityPublisher(ObjectMapper om, EntityIdLists entityIdLists) {
        this.om = om;
        this.entityIdLists = entityIdLists;
    }

    public <T> HttpResponse saveEntitySchema(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("%s/api/treeNode/model/import/JSON/SAMPLE_DATA/%s/%s", host, model, MODEL_VERSION);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        httpPost.setHeader("Authorization", "Bearer " + token);
        httpPost.setHeader("Content-Type", "application/json");

//        TODO a selector to choose with/without root entity
//        StringEntity entity = new StringEntity(convertToJsonAddingRootNode(entities), ContentType.APPLICATION_JSON);
        StringEntity entity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        logger.info(om.writeValueAsString(httpPost.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return response;
        }
    }

    //    entities provided in order to define the model
    public <T> HttpResponse lockEntitySchema(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("%s/api/treeNode/model/%s/%s/lock", host, model, MODEL_VERSION);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        logger.info(om.writeValueAsString(httpPut.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return response;
        }
    }

    public <T> HttpResponse saveEntities(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("%s/api/entity/new/JSON/TREE/%s/%s", host, model, MODEL_VERSION);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

//        TODO a selector to choose with/without root entity
//        choose whether to add a root node to a list of entities, creates an extra parent entity in saas, enables view siblings

//        StringEntity requestEntity = new StringEntity(convertToJsonAddingRootNode(entities), ContentType.APPLICATION_JSON);
        StringEntity requestEntity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);

        logger.info("SAVE ENTITY REQUEST: " + om.writeValueAsString(httpPost.toString()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity);
            JsonNode jsonNode = om.readTree(responseBody);
            logger.info("SAVE ENTITY RESPONSE: " + jsonNode.toString());

//            List<UUID> entityIdList = new ArrayList<>();
//            for (JsonNode idNode : jsonNode) {
//                var idMap = idNode.get("entityIds");
//                entityIdList.add(UUID.fromString(idMap.get(0).asText()));
            List<UUID> entityIdList = new ArrayList<>();
            for (JsonNode idNode : jsonNode) {
                var idMap = idNode.get("entityIds");
                idMap.forEach(node -> entityIdList.add(UUID.fromString(node.asText())));
            }

            switch (model) {
                case "expense_report_nested", "expense_report":
                    waitForIdCollection();
//                    entityIdLists.addToExpenseReportIdList(entityIdList);
//                    logger.info(model + "IdList updated with ids: " + entityIdList);
                    break;
                case "payment":
                    entityIdLists.addToPaymentIdList(entityIdList);
                    logger.info(model + "IdList updated with ids: " + entityIdList);
                    break;
                case "employee":
                    entityIdLists.addToEmployeeIdList(entityIdList);
                    logger.info(model + "IdList updated with ids: " + entityIdList);
                    break;
                default:
                    logger.warn("No corresponding entity model found");
                    break;
            }

            return response;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    public <T> HttpResponse deleteAllEntitiesByModel(List<T> entity) throws IOException {
        String modelName = getModelForClass(entity);
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

    public <T> String convertToJsonAddingRootNode(List<T> entities) throws Exception{
        ObjectNode rootNode = om.createObjectNode();
        ObjectNode dataNode = rootNode.putObject("data");

        ArrayNode reportArray = om.valueToTree(entities);
        String dataModel = getModelForClass(entities);
        dataNode.set(dataModel, reportArray);

        return rootNode.toString();
    }

    public <T> String getModelForClass(List<T> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        Class<?> firstClass = entities.get(0).getClass();
        return switch (firstClass.getSimpleName()) {
            case "ExpenseReport" -> "expense_report";
            case "ExpenseReportNested" -> "expense_report_nested";
            case "Payment" -> "payment";
            case "Employee" -> "employee";
            default -> "unknown_model";
        };
    }

    public void waitForIdCollection() throws InterruptedException {
        int maxWaitTimeInMillis = 2000;
        int waitIntervalInMillis = 400;

        int waitedTime = 0;
        while (entityIdLists.getExpenseReportIdList().isEmpty() && waitedTime < maxWaitTimeInMillis * 3) {
            TimeUnit.MILLISECONDS.sleep(waitIntervalInMillis);
            waitedTime += waitIntervalInMillis;
        }

        if (entityIdLists.getExpenseReportIdList().isEmpty()) {
            throw new IllegalStateException("Timeout: entityIdList is still empty after waiting.");
        }
    }
}
