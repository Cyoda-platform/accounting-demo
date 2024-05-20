package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ExpenseReportNested {

    @JsonIgnore
    private UUID id;
    private UUID employeeId;
    private String city;
    private Timestamp departureDate;
    private List<Expense> expenseList;
    private String totalAmount;

    @Override
    public String toString() {
        return "ExpenseReport{" +
                "id=" + id +
                ", employeeId='" + employeeId + '\'' +
                ", city='" + city + '\'' +
                ", departureDate=" + departureDate +
                ", totalAmount='" + totalAmount + '\'' +
                '}';
    }
}
