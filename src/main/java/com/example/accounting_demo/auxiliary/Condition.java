package com.example.accounting_demo.auxiliary;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Condition {
    private String type;
    private String jsonPath;
    private String operatorType;
    private String value;

    public Condition(String type, String jsonPath, String operatorType, String value) {
        this.type = type;
        this.jsonPath = jsonPath;
        this.operatorType = operatorType;
        this.value = value;
    }
}
