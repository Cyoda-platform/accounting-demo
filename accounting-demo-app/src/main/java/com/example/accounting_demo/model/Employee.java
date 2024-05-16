package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Employee {
    @JsonIgnore
    private UUID id;
    private String fullName;
    private String department;

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
