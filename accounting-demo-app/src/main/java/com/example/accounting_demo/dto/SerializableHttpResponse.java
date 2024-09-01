package com.example.accounting_demo.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class SerializableHttpResponse implements Serializable {
    private int statusCode;
    private String responseBody;

    public SerializableHttpResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {return "";}
}

