package com.example.accounting_demo.service;

import com.example.accounting_demo.dto.SerializableHttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RmiEntityServiceTest {

    @Autowired
    private EntityService entityServiceImpl;

    ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void deleteAllEntitiesAndModels() throws Exception {
        entityServiceImpl.deleteAllEntitiesByModel("test");
        entityServiceImpl.deleteEntityModel("test");
    }

    @AfterEach
    void deleteEntitiesAndModels() throws Exception {
        entityServiceImpl.deleteAllEntitiesByModel("test");
        entityServiceImpl.deleteEntityModel("test");
    }

    @Test
    public void testRmi() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            RmiEntityService server = (RmiEntityService) registry.lookup("RmiEntityService");
            String entity = "{\"fullName\":\"Bruce Gleason\",\"department\":\"Marketing\"}";

            SerializableHttpResponse responseSaveModel = server.saveEntityModel(entity, "test", "1");
            assertThat(responseSaveModel.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

            SerializableHttpResponse responseLock = server.lockEntityModel("test", "1");
            assertThat(responseLock.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

            SerializableHttpResponse responseSaveEntity = server.saveEntity(entity, "test", "1");
            assertThat(responseSaveEntity.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

            SerializableHttpResponse responseGetAll = server.getAllEntitiesAsJson("test", "1");
            assertThat(responseGetAll.getResponseBody().contains("{\"fullName\":\"Bruce Gleason\",\"department\":\"Marketing\"}"));

            JsonNode jsonNode = om.readTree(responseGetAll.getResponseBody());
            if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                JsonNode firstElement = jsonNode.get(0);
                String id = firstElement.get("id").asText();
                SerializableHttpResponse responseGetById = server.getByIdAsJson(UUID.fromString(id));
                assertThat(responseGetById.getResponseBody().contains("{\"fullName\":\"Bruce Gleason\",\"department\":\"Marketing\"}"));
            } else {
                throw new RuntimeException("no entity found");
            }

        } catch (RemoteException | NotBoundException e) {
            fail("Exception Occurred: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
