package com.example.accounting_demo.service;

import com.example.accounting_demo.auxiliary.*;
import com.example.accounting_demo.model.ExpenseReportNested;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EntityServiceTest {

    @Autowired
    private EntityGenerator entityGenerator;

    @Autowired
    private EntityService entityService;

    @Autowired
    private Randomizer random;

    @Autowired
    private JsonToEntityParser jsonToEntityParser;

    private ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void addEntityModels() throws IOException, InterruptedException {
        entityService.deleteAllEntitiesByModel("expense_report");
        entityService.deleteAllEntitiesByModel("expense_report_nested");
        entityService.deleteAllEntitiesByModel("employee");
        entityService.deleteAllEntitiesByModel("payment");

        entityService.deleteEntityModel("expense_report");
        entityService.deleteEntityModel("expense_report_nested");
        entityService.deleteEntityModel("employee");
        entityService.deleteEntityModel("payment");

        var employee = entityGenerator.generateEmployees(1);
        entityService.saveEntitySchema(employee);
        entityService.lockEntitySchema(employee);

        var report = entityGenerator.generateReports(1, true);
        entityService.saveEntitySchema(report);
        entityService.lockEntitySchema(report);

        var report_nested = entityGenerator.generateNestedReports(1, true);
        entityService.saveEntitySchema(report_nested);
        entityService.lockEntitySchema(report_nested);

        var payment = entityGenerator.generatePayments(1);
        entityService.saveEntitySchema(payment);
        entityService.lockEntitySchema(payment);
    }

//    @AfterEach
//    void deleteEntitiesAndModels() throws Exception {
//        entityService.deleteAllEntitiesByModel("expense_report");
//        entityService.deleteAllEntitiesByModel("expense_report_nested");
//        entityService.deleteAllEntitiesByModel("employee");
//        entityService.deleteAllEntitiesByModel("payment");
//
//        entityService.deleteEntityModel("expense_report");
//        entityService.deleteEntityModel("expense_report_nested");
//        entityService.deleteEntityModel("employee");
//        entityService.deleteEntityModel("payment");
//    }

    @Test
    public void saveEmployeeListTest() throws Exception {
        var employees = entityGenerator.generateEmployees(5);
        HttpResponse response = entityService.saveEntities(employees);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void saveExpenseReportListTest() throws Exception {
        var reports = entityGenerator.generateReports(5);
        HttpResponse response = entityService.saveEntities(reports);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void saveNestedExpenseReportListTest() throws Exception {
        var reports = entityGenerator.generateNestedReports(5);
        HttpResponse response = entityService.saveEntities(reports);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void savePaymentListTest() throws Exception {
        var payments = entityGenerator.generatePayments(5);
        HttpResponse response = entityService.saveEntities(payments);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void launchTransitionTest() throws Exception {
        var report = entityGenerator.generateReports(1);
        HttpResponse response1 = entityService.saveEntities(report);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var reportFromDb = entityService.getAllEntitiesByModel("expense_report", "1");
        assertThat(reportFromDb).hasSize(1);
        var savedReportId = reportFromDb.get(0).getId();

        var statusBeforeTransition = entityService.getCurrentState(savedReportId);

        HttpResponse response2 = entityService.launchTransition(savedReportId, "SUBMIT");
        int statusCode2 = response2.getStatusLine().getStatusCode();
        var statusAfterTransition = entityService.getCurrentState(savedReportId);

        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);
        assertThat(statusBeforeTransition).isNotEqualTo(statusAfterTransition);
    }

    @Test
    public void getValueTest() throws Exception {
        var report = entityGenerator.generateReports(1);
        HttpResponse response = entityService.saveEntities(report);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        var reportFromDb = entityService.getAllEntitiesByModel("expense_report", "1");
        assertThat(reportFromDb).hasSize(1);
        var savedReportId = reportFromDb.get(0).getId();
        String columnPath = "strings.[.city]";

        var value = entityService.getValue(savedReportId, columnPath);

        assertThat(value).isNotNull();
    }

    @Test
    public void updateValueTest() throws Exception {
        var report = entityGenerator.generateReports(1);
        HttpResponse response1 = entityService.saveEntities(report);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        String columnPath = "strings.[.city]";

        var reportFromDb = entityService.getAllEntitiesByModel("expense_report", "1");
        assertThat(reportFromDb).hasSize(1);
        var savedReportId = reportFromDb.get(0).getId();
        String updatedValue = "updatedCity";

        JsonNode jsonNode = om.valueToTree(updatedValue);

        HttpResponse response2 = entityService.updateValue(savedReportId, columnPath, jsonNode);

        var currentValue = entityService.getValue(savedReportId, columnPath);

        assertThat(response2.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(currentValue).isEqualTo(updatedValue);
    }

    @Test
    public void deleteTest() throws Exception {
        var employeeList = entityGenerator.generateEmployees(1);
        HttpResponse response1 = entityService.saveEntities(employeeList);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        HttpResponse response2 = entityService.deleteAllEntitiesByModel(employeeList);

        assertThat(response2.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void deleteEntityByRootId() throws Exception {

        var nEmployees = 1;
        var nReports = 1;

        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        var reports = entityGenerator.generateNestedReports(nReports);
        entityService.saveEntities(reports);

        var reportFromDb = entityService.getAllEntitiesByModel("expense_report_nested", "1");
        assertThat(reportFromDb).hasSize(1);
        var savedReportId = reportFromDb.get(0).getId();

        HttpResponse response1 = entityService.deleteEntityByRootId("expense_report_nested", "1", savedReportId.toString());
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void searchResultTest() throws Exception {
        var employees = entityGenerator.generateEmployees(1);
        entityService.saveEntities(employees);

        var nReports = 5;
        var reports = entityGenerator.generateNestedReports(nReports);
        var report1 = reports.get(0);
        report1.setCity("FirstCity");
        var report2 = reports.get(1);
        report2.setCity("SecondCity");

        entityService.saveEntities(reports);

        var conditionRequest = new SearchConditionRequest();
        conditionRequest.setType("group");
        conditionRequest.setOperator("OR");
        conditionRequest.setConditions(List.of(
                new Condition("simple", "$.city", "EQUALS", "FirstCity"),
                new Condition("simple", "$.city", "EQUALS", "SecondCity")
        ));
        var searchId = entityService.runSearchAndGetSnapshotId("expense_report_nested", "1", conditionRequest);

        var status = entityService.getSnapshotStatus(searchId);
        assertThat(status.get("snapshotStatus")).isEqualTo("SUCCESSFUL");

        String json = entityService.getSearchResultAsJson(searchId);
        var entities = jsonToEntityParser.parseResponse(json, ExpenseReportNested.class);
        assertThat(entities.size()).isEqualTo(2);
    }

    @Test
    public void getAllEntitiesByModel() throws Exception {
        var nEmployees = 3;
        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        var employeesFromDb = entityService.getAllEntitiesByModel("employee", "1");
        assertThat(employeesFromDb.size()).isEqualTo(nEmployees);

        var nReports = 5;
        var reports = entityGenerator.generateNestedReports(nReports);
        entityService.saveEntities(reports);
        var reportsFromDb = entityService.getAllEntitiesByModel("expense_report_nested", "1");
        assertThat(reportsFromDb.size()).isEqualTo(nReports);
    }

    @Test
    public void flatEntitiesWorkflowTest() throws Exception {
        var nEmployees = 10;
        var nReports = 40;
        var nTransitions = 200;

        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        //        ExpenseReport is created by an employee
        var reports = entityGenerator.generateReports(nReports);
        entityService.saveEntities(reports);

        var reportsFromDb = entityService.getAllEntitiesByModel("expense_report", "1");
        assertThat(reportsFromDb).hasSize(nReports);

        //select a random ExpenseReport and run a random available transition, then take another one and repeat

        for (int i = 0; i < nTransitions; i++) {
            System.out.println("\nTRANSITION NUMBER: " + i);

            var randomReportId = random.getRandomElement(reportsFromDb).getId();
            var availableTransitions = entityService.getListTransitions(randomReportId);
            System.out.println("Available transitions: " + availableTransitions.toString());

            try {
                if (!availableTransitions.isEmpty()) {
                    var randomTransition = random.getRandomElement(availableTransitions);
                    System.out.println("Transition chosen to run: " + randomTransition);

                    switch (randomTransition) {
                        case "UPDATE":
//            TODO add generating random fields AND update updateValue method to take a list of changes
                            String columnPath = "strings.[.city]";
                            String updatedValue = "updatedCity";
                            JsonNode jsonNode = om.valueToTree(updatedValue);
                            entityService.updateValue(randomReportId, columnPath, jsonNode);
                            break;
                        case "POST_PAYMENT":
//                        should be launched by an externalized processor - payment transition "ACCEPT_BY_BANK"
                            break;
                        default:
                            entityService.launchTransition(randomReportId, randomTransition);
                            break;
                    }
                }
                // added delay to let externalized processor run and finish before next transition cycle
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted: " + e.getMessage());
            }
        }
    }

    @Test
    public void nestedEntitiesWorkflowTest() throws Exception {
        var nEmployees = 5;
        var nReports = 20;
        var nTransitions = 30;

        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        //        ExpenseReport is created by an employee
        var reports = entityGenerator.generateNestedReports(nReports);
        entityService.saveEntities(reports);

        var reportsFromDb = entityService.getAllEntitiesByModel("expense_report_nested", "1");
        assertThat(reportsFromDb).hasSize(nReports);

        //select a random ExpenseReport and run a random available transition, then take another one and repeat

        for (int i = 0; i < nTransitions; i++) {
            System.out.println("\nTRANSITION NUMBER: " + i);

            var randomReportId = random.getRandomElement(reportsFromDb).getId();
            var availableTransitions = entityService.getListTransitions(randomReportId);
            System.out.println("Available transitions: " + availableTransitions.toString());

            try {
                if (!availableTransitions.isEmpty()) {
                    var randomTransition = random.getRandomElement(availableTransitions);
                    System.out.println("Transition chosen to run: " + randomTransition);

                    switch (randomTransition) {
                        case "UPDATE":
//            TODO add generating random fields AND update updateValue method to take a list of changes
                            String columnPath = "strings.[.city]";
                            String updatedValue = "updatedCity";
                            JsonNode jsonNode = om.valueToTree(updatedValue);
                            entityService.updateValue(randomReportId, columnPath, jsonNode);
                            break;
                        case "POST_PAYMENT":
//                        should be launched by an EP - payment transition "ACCEPT_BY_BANK"
                            break;
                        default:
                            entityService.launchTransition(randomReportId, randomTransition);
                            break;
                    }
                }
// added delay to let externalized processor run and finish before next transition cycle
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted: " + e.getMessage());
            }
        }
    }
}