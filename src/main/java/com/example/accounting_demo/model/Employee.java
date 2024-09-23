package com.example.accounting_demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(super.getId(), employee.getId()) &&
                Objects.equals(fullName, employee.fullName) &&
                Objects.equals(department, employee.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, department);
    }
}
