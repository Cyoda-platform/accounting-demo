package com.example.accounting_demo.auxiliary;

import com.example.accounting_demo.model.BaseEntity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JsonToEntityParser {
    public <T extends BaseEntity> List<T> parseResponse(String jsonResponse, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmbeddedWrapper response = mapper.readValue(jsonResponse, EmbeddedWrapper.class);
        List<EmbeddedWrapper.EntityWrapper> entityWrappers = response.getEmbedded().getObjectNodes();

        List<T> entities = new ArrayList<>();
        for (EmbeddedWrapper.EntityWrapper wrapper : entityWrappers) {
            T entity = mapper.convertValue(wrapper.getEntity(), clazz);
            entity.setId(UUID.fromString(wrapper.getId()));
            entities.add(entity);
        }
        return entities;
    }
}
