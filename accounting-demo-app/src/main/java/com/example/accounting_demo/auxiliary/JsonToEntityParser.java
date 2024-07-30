package com.example.accounting_demo.auxiliary;

import com.example.accounting_demo.model.BaseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JsonToEntityParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonToEntityParser.class);

    public <T extends BaseEntity> List<T> parseResponse(String jsonResponse, Class<T> clazz) throws IOException {
        List<T> entities = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            List<EmbeddedWrapper.TreeWrapper> treeWrappers = mapper.readValue(jsonResponse, new TypeReference<List<EmbeddedWrapper.TreeWrapper>>() {
            });
            for (EmbeddedWrapper.TreeWrapper wrapper : treeWrappers) {
                T entity = mapper.convertValue(wrapper.getTree(), clazz);
                entity.setId(UUID.fromString(wrapper.getId()));
                entities.add(entity);
            }
        } catch (JsonProcessingException e) {
            try {
                EmbeddedWrapper response = mapper.readValue(jsonResponse, EmbeddedWrapper.class);
                List<EmbeddedWrapper.TreeWrapper> treeWrappers = response.getEmbedded().getObjectNodes();
                for (EmbeddedWrapper.TreeWrapper wrapper : treeWrappers) {
                    T entity = mapper.convertValue(wrapper.getTree(), clazz);
                    entity.setId(UUID.fromString(wrapper.getId()));
                    entities.add(entity);
                }
            } catch (JsonProcessingException ex) {
                logger.warn("failed parsing json to entity");
            }
        }

        return entities;
    }
}
