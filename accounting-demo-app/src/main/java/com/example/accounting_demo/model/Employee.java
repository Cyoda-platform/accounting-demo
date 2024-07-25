package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Employee extends BaseEntity {

    private String fullName;
    private String department;

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + super.getId() +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
