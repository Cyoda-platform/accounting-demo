package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
public class ExpenseReport extends BaseEntity {

    private UUID employeeId;
    private String city;
    private Timestamp departureDate;
    private String totalAmount;

    @Override
    public String toString() {
        return "ExpenseReport{" +
                "id=" + super.getId() +
                ", employeeId='" + employeeId + '\'' +
                ", city='" + city + '\'' +
                ", departureDate=" + departureDate +
                ", totalAmount='" + totalAmount + '\'' +
                '}';
    }
}
