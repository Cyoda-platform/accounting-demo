package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class ExpenseReport extends BaseEntity {

    private UUID employeeId;
    private String destination;
    private Timestamp departureDate;
    private List<Expense> expenseList;
    private BigDecimal amountPayable;

    @Override
    public String toString() {
        return "ExpenseReport{" +
                "id=" + super.getId() +
                ", employeeId='" + employeeId + '\'' +
                ", destination='" + destination + '\'' +
                ", departureDate=" + departureDate +
                ", expenseList=" + expenseList +
                ", amountPayable='" + amountPayable + '\'' +
                '}';
    }

    public void setAmountPayable(BigDecimal amountPayable) {
        this.amountPayable = amountPayable.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseReport report = (ExpenseReport) o;
        return Objects.equals(super.getId(), report.getId()) &&
                Objects.equals(employeeId, report.employeeId) &&
                Objects.equals(destination, report.destination) &&
                Objects.equals(departureDate, report.departureDate) &&
                Objects.equals(expenseList, report.expenseList) &&
                Objects.equals(amountPayable, report.amountPayable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeId, destination, departureDate, expenseList, amountPayable);
    }
}
