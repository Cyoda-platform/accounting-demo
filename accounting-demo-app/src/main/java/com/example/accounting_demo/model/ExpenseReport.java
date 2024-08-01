package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseReport report = (ExpenseReport) o;
        return Objects.equals(super.getId(), report.getId()) &&
                Objects.equals(employeeId, report.employeeId) &&
                Objects.equals(city, report.city) &&
                Objects.equals(departureDate, report.departureDate) &&
                Objects.equals(totalAmount, report.totalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeId, city, departureDate, totalAmount);
    }
}
