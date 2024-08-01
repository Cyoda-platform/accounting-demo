package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseReportNested report = (ExpenseReportNested) o;
        return Objects.equals(super.getId(), report.getId()) &&
                Objects.equals(employeeId, report.employeeId) &&
                Objects.equals(city, report.city) &&
                Objects.equals(departureDate, report.departureDate) &&
                Objects.equals(expenseList, report.expenseList) &&
                Objects.equals(totalAmount, report.totalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeId, city, departureDate, expenseList, totalAmount);
    }
}
