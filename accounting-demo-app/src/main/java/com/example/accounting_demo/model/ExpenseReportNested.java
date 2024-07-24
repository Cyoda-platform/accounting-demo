package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ExpenseReportNested extends BaseEntity {

    private UUID employeeId;
    private String city;
    private Timestamp departureDate;
    private List<Expense> expenseList;
    private String totalAmount;

    @Override
    public String toString() {
        return "ExpenseReportNested{" +
                "id=" + super.getId() +
                ", employeeId='" + employeeId + '\'' +
                ", city='" + city + '\'' +
                ", departureDate=" + departureDate +
                ", expenseList=" + expenseList +
                ", totalAmount='" + totalAmount + '\'' +
                '}';
    }
}
