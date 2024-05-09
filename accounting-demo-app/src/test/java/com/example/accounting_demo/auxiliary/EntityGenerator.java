package com.example.accounting_demo.auxiliary;

import com.example.accounting_demo.model.BusinessTravelReport;
import com.example.accounting_demo.model.Payment;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class EntityGenerator {

    @Autowired
    private Faker faker;

    public List<BusinessTravelReport> generateReport(int count) {
        List<BusinessTravelReport> reports = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var report = Instancio.of(BusinessTravelReport.class)
                    .ignore(Select.field(BusinessTravelReport::getId))
                    .supply(Select.field(BusinessTravelReport::getEmployeeName), () -> faker.name().fullName())
                    .supply(Select.field(BusinessTravelReport::getCity), () -> faker.country().capital())
                    .supply(Select.field(BusinessTravelReport::getDepartureDate), () -> faker.date().past(1, TimeUnit.DAYS))
                    .supply(Select.field(BusinessTravelReport::getTotalAmount), () -> faker.commerce().price(10, 1000))
                    .create();
            reports.add(report);
        }
        return reports;
    }

    public List<BusinessTravelReport> generateReport() {
        return generateReport(1);
    }

    public List<Payment> generatePayment() {
        List<Payment> payments = new ArrayList<>();
        var payment = Instancio.of(Payment.class)
                .ignore(Select.field(Payment::getId))
                .supply(Select.field(Payment::getBtReportId), () -> UUID.fromString("a50a7fbe-1e3b-11b2-9575-f2bfe09fbe21"))
                .supply(Select.field(Payment::getAmount), () -> faker.commerce().price(10, 1000))
                .create();
        payments.add(payment);
        return payments;
    }
}
