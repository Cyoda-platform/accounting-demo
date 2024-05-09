package com.example.accounting_demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class EntityPublisher {

    @Autowired
    ObjectMapper mapper;

    private String modelVersion = "1";
    private final HttpClient httpClient = HttpClients.createDefault();

    @Value("${my.token}")
    private String token;

    public <T> HttpResponse saveEntitySchema(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("http://localhost:8082/api/treeNode/model/import/JSON/SAMPLE_DATA/%s/%s", model, modelVersion);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Authorization", "Bearer " + token);
        httpPost.setHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(convertToJson(entities), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        System.out.println(httpPost);

        return httpClient.execute(httpPost);
    }

//    entities provided in order to define the model
    public <T> HttpResponse lockEntitySchema(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("http://localhost:8082/api/treeNode/model/%s/%s/lock", model, modelVersion);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Bearer " + token);

        System.out.println(httpPut);

        return httpClient.execute(httpPut);
    }

    public <T> HttpResponse saveEntities(List<T> entities) throws IOException {
        String model = getModelForClass(entities);

        String url = String.format("http://localhost:8082/api/entity/new/JSON/TREE/%s/%s", model, modelVersion);
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);

        StringEntity entity = new StringEntity(convertToJson(entities), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        System.out.println(httpPost);

        return httpClient.execute(httpPost);
    }

    public <T> String convertToJson(List<T> entities) {
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode dataNode = rootNode.putObject("data");

        ArrayNode reportArray = mapper.valueToTree(entities);
        String dataModel = getModelForClass(entities);
        dataNode.set(dataModel, reportArray);

        System.out.println(rootNode);

        return rootNode.toString();
    }

    //create enum for entity models?
    public <T> String getModelForClass(List<T> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        Class<?> firstClass = entities.get(0).getClass();
        return switch (firstClass.getSimpleName()) {
            case "BusinessTravelReport" -> "travel_report";
            case "Payment" -> "payment";
            default -> "unknown_model";
        };
    }
}
