package com.example.accounting_demo.service;

import com.example.accounting_demo.auxiliary.EntityGenerator;
import com.example.accounting_demo.auxiliary.Randomizer;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EntityServiceTest {

    @Autowired
    private EntityGenerator entityGenerator;

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntityIdLists entityIdLists;

    @Autowired
    private Randomizer random;

    private ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void addEntityModels() throws IOException {
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

        var report = entityGenerator.generateReports(1);
        entityService.saveEntitySchema(report);
        entityService.lockEntitySchema(report);

        var report_nested = entityGenerator.generateNestedReports(1);
        entityService.saveEntitySchema(report_nested);
        entityService.lockEntitySchema(report_nested);

        var payment = entityGenerator.generatePayments(1);
        entityService.saveEntitySchema(payment);
        entityService.lockEntitySchema(payment);
    }

    @AfterEach
    void deleteEntitiesAndModels() throws Exception {
        entityService.deleteAllEntitiesByModel("expense_report");
        entityService.deleteAllEntitiesByModel("expense_report_nested");
        entityService.deleteAllEntitiesByModel("employee");
        entityService.deleteAllEntitiesByModel("payment");

        entityService.deleteEntityModel("expense_report");
        entityService.deleteEntityModel("expense_report_nested");
        entityService.deleteEntityModel("employee");
        entityService.deleteEntityModel("payment");
    }

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

        var savedReportId = entityIdLists.getExpenseReportIdList().get(0);
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

        var savedReportId = entityIdLists.getExpenseReportIdList().get(0);
        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.city]";

        var value = entityService.getValue(savedReportId, columnPath);

        assertThat(value).isNotNull();
    }

    @Test
    public void updateValueTest() throws Exception {
        var report = entityGenerator.generateReports(1);
        HttpResponse response1 = entityService.saveEntities(report);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.city]";

        var savedReportId = entityIdLists.getExpenseReportIdList().get(0);
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

        var id = entityIdLists.getRandomExpenseReportId().toString();

        HttpResponse response1 = entityService.deleteEntityByRootId("expense_report_nested", "1", id);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
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

        //select a random ExpenseReport and run a random available transition, then take another one and repeat

        for (int i = 0; i < nTransitions; i++) {
            System.out.println("TRANSITION NUMBER: " + i);

            var randomReportId = entityIdLists.getRandomExpenseReportId();
            var availableTransitions = entityService.getListTransitions(randomReportId);
            System.out.println("Available transitions: " + availableTransitions.toString());

            if (!availableTransitions.isEmpty()) {
                var randomTransition = random.getRandomElement(availableTransitions);
                System.out.println("Transition chosen to run: " + randomTransition);

                switch (randomTransition) {
                    case "UPDATE":
//            TODO add generating random fields AND update updateValue method to take a list of changes
                        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.city]";
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
        }
    }

    @Test
    public void nestedEntitiesWorkflowTest() throws Exception {
        var nEmployees = 5;
        var nReports = 15;
        var nTransitions = 50;

        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        //        ExpenseReport is created by an employee
        var reports = entityGenerator.generateNestedReports(nReports);
        entityService.saveEntities(reports);

        //select a random ExpenseReport and run a random available transition, then take another one and repeat

        for (int i = 0; i < nTransitions; i++) {
            System.out.println("TRANSITION NUMBER: " + i);

            var randomReportId = entityIdLists.getRandomExpenseReportId();
            var availableTransitions = entityService.getListTransitions(randomReportId);
            System.out.println("Available transitions: " + availableTransitions.toString());

            if (!availableTransitions.isEmpty()) {
                var randomTransition = random.getRandomElement(availableTransitions);
                System.out.println("Transition chosen to run: " + randomTransition);

                switch (randomTransition) {
                    case "UPDATE":
//            TODO add generating random fields AND update updateValue method to take a list of changes
                        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.city]";
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
        }
    }
}