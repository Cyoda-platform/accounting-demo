package com.example.accounting_demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Service
public class Authentication {

    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);

    @Value("${cyoda.host}")
    private String host;

    @Value("${cyoda.name}")
    private String name;

    @Value("${cyoda.password}")
    private String password;

    private String token;

    private final ObjectMapper om;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public Authentication(ObjectMapper om) {
        this.om = om;
    }

    @PostConstruct
    public void authenticate() throws Exception {
        String url = String.format("%s/api/auth/login", host);
        HttpPost httpPost = new HttpPost(url);

        String jsonPayload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", name, password);
        StringEntity entity = new StringEntity(jsonPayload);

        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        HttpResponse response = httpClient.execute(httpPost);

        HttpEntity responseEntity = response.getEntity();
        logger.info("Authentication response code: {}", response.getStatusLine().getStatusCode());
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");

        JsonNode rootNode = om.readTree(responseString);
        token = rootNode.path("token").asText();

        httpClient.close();
    }

    public String getToken() {
        return token;
    }
}
