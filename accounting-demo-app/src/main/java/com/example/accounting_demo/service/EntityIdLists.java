package com.example.accounting_demo.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Getter
@Setter
public class EntityIdLists {

    private List<UUID> employeeIdList = new ArrayList<>();
    private List<UUID> travelReportIdList = new ArrayList<>();
    private List<UUID> paymentIdList = new ArrayList<>();

    private static final Random random = new Random();
    
    public void addToEmployeeIdList(List<UUID> ids) {
        employeeIdList.addAll(ids);
    }

    public void addToTravelReportIdList(List<UUID> ids) {
        travelReportIdList.addAll(ids);
    }

    public void addToPaymentIdList(List<UUID> ids) {
        paymentIdList.addAll(ids);
    }

    public UUID getRandomEmployeeId() {
        if (employeeIdList.isEmpty()) {
            return null;
        }
        return employeeIdList.get(random.nextInt(employeeIdList.size()));
    }

    public UUID getRandomTravelReportId() {
        if (travelReportIdList.isEmpty()) {
            return null;
        }
        return travelReportIdList.get(random.nextInt(travelReportIdList.size()));
    }

    public UUID getRandomPaymentId() {
        if (paymentIdList.isEmpty()) {
            return null;
        }
        return paymentIdList.get(random.nextInt(paymentIdList.size()));
    }

}
