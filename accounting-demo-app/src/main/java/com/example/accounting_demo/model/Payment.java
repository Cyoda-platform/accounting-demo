package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Payment {

    @JsonIgnore
    private UUID id;
    private UUID btReportId;
    private String amount;
}
