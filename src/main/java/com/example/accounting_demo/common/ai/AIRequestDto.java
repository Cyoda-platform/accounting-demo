package com.example.accounting_demo.common.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class AIRequestDto {
    private String id = UUID.randomUUID().toString();
    private String question;
    private String return_object = "chat";
}
