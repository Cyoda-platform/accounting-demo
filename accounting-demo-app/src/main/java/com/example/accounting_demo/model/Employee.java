package com.example.accounting_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Employee {
    @Id
    @JsonIgnore
    private UUID id;
    private String fullName;
    private String department;

    @Override
    public String toString() {
        return "BusinessTravelReport{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
