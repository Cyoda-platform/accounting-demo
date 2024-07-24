package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Expense {
    private String description;
    private String amount;

    @Override
    public String toString() {
        return "Expense{" +
                "description='" + description + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
