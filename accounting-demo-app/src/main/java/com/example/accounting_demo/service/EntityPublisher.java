package com.example.accounting_demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EntityPublisher {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    EntityIdLists entityIdLists;

    private String modelVersion = "1";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Value("${my.token}")
    private String token;

    public <T> HttpResponse saveEntitySchema(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("http://localhost:8082/api/treeNode/model/import/JSON/SAMPLE_DATA/%s/%s", model, modelVersion);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Authorization", "Bearer " + token);
        httpPost.setHeader("Content-Type", "application/json");

//        TODO a selector to choose with/without root entity
//        StringEntity entity = new StringEntity(convertToJsonAddingRootNode(entities), ContentType.APPLICATION_JSON);
        StringEntity entity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        System.out.println(httpPost);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return response;
        }
    }

    //    entities provided in order to define the model
    public <T> HttpResponse lockEntitySchema(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("http://localhost:8082/api/treeNode/model/%s/%s/lock", model, modelVersion);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        System.out.println(httpPut);

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return response;
        }
    }

    public <T> HttpResponse saveEntities(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("http://localhost:8082/api/entity/new/JSON/TREE/%s/%s", model, modelVersion);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

//        TODO a selector to choose with/without root entity
//        choose whether to add a root node to a list of entities, creates an extra parent entity in saas, enables view siblings

//        StringEntity requestEntity = new StringEntity(convertToJsonAddingRootNode(entities), ContentType.APPLICATION_JSON);
        StringEntity requestEntity = new StringEntity(convertListToJson(entities), ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);

        System.out.println(httpPost);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity);
            JsonNode jsonNode = mapper.readTree(responseBody);
            JsonNode entityIdsNode = jsonNode.get(0).get("entityIds");

            List<UUID> entityIdList = new ArrayList<>();
            for (JsonNode idNode : entityIdsNode) {
                entityIdList.add(UUID.fromString(idNode.asText()));
            }

            switch (model) {
                case "travel_report":
                    entityIdLists.addToTravelReportIdList(entityIdList);
                    System.out.println(model + "IdList updated with ids: " + entityIdList);
                    break;
                case "payment":
                    entityIdLists.addToPaymentIdList(entityIdList);
                    System.out.println(model + "IdList updated with ids: " + entityIdList);
                    break;
                case "employee":
                    entityIdLists.addToEmployeeIdList(entityIdList);
                    System.out.println(model + "IdList updated with ids: " + entityIdList);
                    break;
                default:
                    System.out.println("No corresponding entity model found");
                    break;
            }

            return response;
        }
    }

    public <T> String convertListToJson(List<T> entities) throws JsonProcessingException {
        return mapper.writeValueAsString(entities);
    }

    public <T> String convertToJsonAddingRootNode(List<T> entities) {
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode dataNode = rootNode.putObject("data");

        ArrayNode reportArray = mapper.valueToTree(entities);
        String dataModel = getModelForClass(entities);
        dataNode.set(dataModel, reportArray);

        System.out.println(rootNode);

        return rootNode.toString();
    }

    public <T> String getModelForClass(List<T> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        Class<?> firstClass = entities.get(0).getClass();
        return switch (firstClass.getSimpleName()) {
            case "BusinessTravelReport" -> "travel_report";
            case "Payment" -> "payment";
            case "Employee" -> "employee";
            default -> "unknown_model";
        };
    }
}
