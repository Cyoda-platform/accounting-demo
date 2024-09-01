package com.example.accounting_demo.service;

import com.example.accounting_demo.dto.SerializableHttpResponse;

import java.io.IOException;
import java.rmi.Remote;
import java.util.UUID;

public interface RmiEntityService extends Remote {

    SerializableHttpResponse saveEntityModel(String entity, String model, String version) throws IOException;
    SerializableHttpResponse lockEntityModel(String model, String version) throws IOException;
    SerializableHttpResponse deleteEntityModel(String modelName, String modelVersion) throws IOException;

    SerializableHttpResponse saveEntity(String entity, String model, String version) throws IOException;
    SerializableHttpResponse getAllEntitiesAsJson(String model, String version) throws IOException;
    SerializableHttpResponse getAllEntitiesAsJson(String model, String version, String pageSize) throws IOException;
    SerializableHttpResponse getAllEntitiesAsJson(String model, String version, String pageSize, String pageNumber) throws IOException;
    SerializableHttpResponse getByIdAsJson(UUID id) throws IOException;
    SerializableHttpResponse deleteEntityById(String modelName, String modelVersion, String id) throws IOException;
    SerializableHttpResponse deleteAllEntitiesByModel(String modelName, String modelVersion) throws IOException;

    SerializableHttpResponse launchTransition(UUID id, String transition) throws IOException;
    SerializableHttpResponse getListTransitions(UUID id) throws IOException;
    SerializableHttpResponse getCurrentState(UUID id) throws IOException;

}