package com.example.accounting_demo.auxiliary;

import com.example.accounting_demo.model.*;
import com.example.accounting_demo.service.EntityService;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class EntityGenerator {

    @Autowired
    private Faker faker;
    @Autowired
    private Randomizer randomizer;
    @Autowired
    private EntityService entityService;

    UUID fakeUuid = UUID.fromString("a50a7fbe-1e3b-11b2-9575-f2bfe09fbe21");

    List<String> descriptions = List.of("hotel", "taxi", "transportation", "meals", "other");

    public List<ExpenseReport> generateReports(int count, boolean fakeEmployeeId) throws IOException, InterruptedException {
        List<ExpenseReport> reports = new ArrayList<>();

        List<UUID> idList = fakeEmployeeId
                ? List.of(fakeUuid)
                : entityService.getAllEntitiesAsObjects("employee", "1").stream()
                .map(BaseEntity::getId)
                .toList();

        for (int i = 0; i < count; i++) {
            UUID employeeId = randomizer.getRandomElement(idList);

            var report = Instancio.of(ExpenseReport.class)
                    .ignore(Select.field(ExpenseReport::getId))
                    .supply(Select.field(ExpenseReport::getEmployeeId), () -> employeeId)
                    .supply(Select.field(ExpenseReport::getCity), () -> faker.country().capital())
                    .supply(Select.field(ExpenseReport::getDepartureDate), () -> faker.date().past(1, TimeUnit.DAYS))
                    .supply(Select.field(ExpenseReport::getTotalAmount), () -> faker.commerce().price(10, 1000))
                    .create();
            reports.add(report);
        }
        return reports;
    }

    public List<ExpenseReport> generateReports(int count) throws IOException, InterruptedException {
        return generateReports(count, false);
    }

    public List<ExpenseReportNested> generateNestedReports(int count, boolean fakeEmployeeId) throws IOException, InterruptedException {
        List<ExpenseReportNested> reports = new ArrayList<>();

        List<UUID> idList = fakeEmployeeId
                ? List.of(fakeUuid)
                : entityService.getAllEntitiesAsObjects("employee", "1").stream()
                .map(BaseEntity::getId)
                .toList();

        for (int i = 0; i < count; i++) {
            UUID employeeId = randomizer.getRandomElement(idList);

            var report = Instancio.of(ExpenseReportNested.class)
                    .ignore(Select.field(ExpenseReportNested::getId))
                    .supply(Select.field(ExpenseReportNested::getEmployeeId), () -> employeeId)
                    .supply(Select.field(ExpenseReportNested::getCity), () -> faker.country().capital())
                    .supply(Select.field(ExpenseReportNested::getDepartureDate), () -> faker.date().past(1, TimeUnit.DAYS))
                    .supply(Select.field(ExpenseReportNested::getExpenseList), () -> generateExpenseList(2))
                    .supply(Select.field(ExpenseReportNested::getTotalAmount), () -> "0.00")
                    .create();
            reports.add(report);
        }
        return reports;
    }

    public List<ExpenseReportNested> generateNestedReports(int count) throws IOException, InterruptedException {
        return generateNestedReports(count, false);
    }

    //given id is used for setting a new entity schema (Time UUID)
    public List<Payment> generatePayments(int count) {
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var payment = Instancio.of(Payment.class)
                    .ignore(Select.field(Payment::getId))
                    .supply(Select.field(Payment::getExpenseReportId), () -> fakeUuid)
                    .supply(Select.field(Payment::getAmount), () -> faker.commerce().price(10, 1000))
                    .create();
            payments.add(payment);
        }
        return payments;
    }

    public List<Employee> generateEmployees(int count) {
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var employee = Instancio.of(Employee.class)
                    .ignore(Select.field(Employee::getId))
                    .supply(Select.field(Employee::getFullName), () -> faker.name().fullName())
                    .supply(Select.field(Employee::getDepartment), () -> faker.job().field())
                    .create();
            employees.add(employee);
        }
        return employees;
    }

    public List<Expense> generateExpenseList(int count) {
        List<Expense> expenses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var expense = Instancio.of(Expense.class)
                    .supply(Select.field(Expense::getDescription), () -> (randomizer.getRandomElement(descriptions)))
                    .supply(Select.field(Expense::getAmount), () -> faker.commerce().price(10, 100))
                    .create();
            expenses.add(expense);
        }
        return expenses;
    }
}
