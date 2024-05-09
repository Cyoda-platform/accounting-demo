package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @JsonIgnore
    private UUID id;
    private UUID btReportId;
    private String amount;
}
