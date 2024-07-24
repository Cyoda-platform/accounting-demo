package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Payment extends BaseEntity {

    private UUID expenseReportId;
    private String amount;

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + super.getId() +
                ", expenseReportId='" + expenseReportId + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
