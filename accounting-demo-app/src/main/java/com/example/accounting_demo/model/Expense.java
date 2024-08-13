package com.example.accounting_demo.model;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

import java.math.RoundingMode;
import java.util.Objects;

@Getter
@Setter
public class Expense {
    private String description;
    private BigDecimal amount;

    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "description='" + description + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(description, expense.description) &&
                Objects.equals(amount, expense.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, amount);
    }
}
