package com.example.accounting_demo.common.util;

import com.example.accounting_demo.common.repository.BaseEntity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class JsonToEntityParser {

    public <T extends BaseEntity> T parseResponse(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        EmbeddedWrapper.SingleTreeWrapper wrapper = mapper.readValue(jsonResponse, EmbeddedWrapper.SingleTreeWrapper.class);
        String model = wrapper.getMeta().getModelKey().getName();
        Class clazz = ModelRegistry.getClassByModel(model);
        T entity = (T) mapper.convertValue(wrapper.getTree(), clazz);
        entity.setId(UUID.fromString(wrapper.getMeta().getId()));

        return entity;
    }
}
