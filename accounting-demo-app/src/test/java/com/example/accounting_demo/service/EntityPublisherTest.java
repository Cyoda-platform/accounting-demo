package com.example.accounting_demo.service;

import com.example.accounting_demo.auxiliary.EntityGenerator;
import com.example.accounting_demo.auxiliary.Randomizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EntityPublisherTest {

    @Autowired
    private EntityGenerator entityGenerator;

    @Autowired
    private EntityPublisher entityPublisher;

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntityIdLists entityIdLists;

    @Autowired
    private Randomizer random;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void saveEntitySchemaTest() throws Exception {
        var employee = entityGenerator.generateEmployees(1);
        HttpResponse response1 = entityPublisher.saveEntitySchema(employee);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var report = entityGenerator.generateReports(1);
//        var report = entityGenerator.generateNestedReports(1);
        HttpResponse response2 = entityPublisher.saveEntitySchema(report);
        int statusCode2 = response2.getStatusLine().getStatusCode();
        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);

        var payment = entityGenerator.generatePayments(1);
        HttpResponse response3 = entityPublisher.saveEntitySchema(payment);
        int statusCode3 = response3.getStatusLine().getStatusCode();
        assertThat(statusCode3).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void lockEntitySchemaTest() throws Exception {
        var employee = entityGenerator.generateEmployees(1);
        HttpResponse response1 = entityPublisher.lockEntitySchema(employee);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var report = entityGenerator.generateReports(1);
//        var report = entityGenerator.generateNestedReports(1);
        HttpResponse response2 = entityPublisher.lockEntitySchema(report);
        int statusCode2 = response2.getStatusLine().getStatusCode();
        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);

        var payment = entityGenerator.generatePayments(1);
        HttpResponse response3 = entityPublisher.lockEntitySchema(payment);
        int statusCode3 = response3.getStatusLine().getStatusCode();
        assertThat(statusCode3).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void saveEmployeeListTest() throws Exception {
        var employees = entityGenerator.generateEmployees(1);
        HttpResponse response = entityPublisher.saveEntities(employees);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void saveBtReportListTest() throws Exception {
        var reports = entityGenerator.generateReports(2);
        HttpResponse response = entityPublisher.saveEntities(reports);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void savePaymentListTest() throws Exception {
        var payment = entityGenerator.generatePayments(1);
        HttpResponse response = entityPublisher.saveEntities(payment);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void launchTransitionTest() throws Exception {
        var report = entityGenerator.generateReports(1);
        HttpResponse response1 = entityPublisher.saveEntities(report);
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
        HttpResponse response = entityPublisher.saveEntities(report);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        var savedReportId = entityIdLists.getExpenseReportIdList().get(0);
        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.city]";

        var value = entityService.getValue(savedReportId, columnPath);

        assertThat(value).isNotNull();
    }

    @Test
    public void updateValueTest() throws Exception {
        var report = entityGenerator.generateReports(1);
        HttpResponse response1 = entityPublisher.saveEntities(report);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.city]";

        var savedReportId = entityIdLists.getExpenseReportIdList().get(0);
        String updatedValue = "updatedCity";

        JsonNode jsonNode = mapper.valueToTree(updatedValue);

        HttpResponse response2 = entityService.updateValue(savedReportId, columnPath, jsonNode);

        var currentValue = entityService.getValue(savedReportId, columnPath);

        assertThat(response2.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(currentValue).isEqualTo(updatedValue);
    }

    @Test
    public void flatEntitiesWorkflowTest() throws Exception {
        var employeesCount = 5;
        var reportsCount = 20;
        var transitionsCount = 200;

        var employees = entityGenerator.generateEmployees(employeesCount);
        entityPublisher.saveEntities(employees);
        //        ExpenseReport is created by an employee
        var reports = entityGenerator.generateReports(reportsCount);
        entityPublisher.saveEntities(reports);

        //select a random ExpenseReport and run a random available transition, then take another one and repeat

        for (int i = 0; i < transitionsCount; i++) {
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
                        JsonNode jsonNode = mapper.valueToTree(updatedValue);
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

    @Test
    public void nestedEntitiesWorkflowTest() throws Exception {
        var employeesCount = 1;
        var reportsCount = 1;
        var transitionsCount = 10;

        var employees = entityGenerator.generateEmployees(employeesCount);
        entityPublisher.saveEntities(employees);
        //        ExpenseReport is created by an employee
        var reports = entityGenerator.generateNestedReports(reportsCount);
        entityPublisher.saveEntities(reports);

        //select a random ExpenseReport and run a random available transition, then take another one and repeat

        for (int i = 0; i < transitionsCount; i++) {
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
                        JsonNode jsonNode = mapper.valueToTree(updatedValue);
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