package com.example.accounting_demo.service;

import com.example.accounting_demo.auxiliary.*;
import com.example.accounting_demo.model.ExpenseReport;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityServiceTest {

    @Autowired
    private EntityGenerator entityGenerator;

    @Autowired
    private EntityService entityService;

    @Autowired
    private Randomizer random;

    @Autowired
    private JsonToEntityListParser jsonToEntityListParser;

    @BeforeAll
    void setUpBeforeClass() throws Exception {
        entityService.deleteAllEntitiesByModel("expense_report");
        entityService.deleteAllEntitiesByModel("employee");
        entityService.deleteAllEntitiesByModel("payment");

        entityService.deleteEntityModel("expense_report");
        entityService.deleteEntityModel("employee");
        entityService.deleteEntityModel("payment");

        var employee = entityGenerator.generateEmployees(1);
        entityService.saveEntityModel(employee);
        entityService.lockEntityModel(employee);

        var expenseReport = entityGenerator.generateExpenseReports(1, true);
        entityService.saveEntityModel(expenseReport);
        entityService.lockEntityModel(expenseReport);

        var payment = entityGenerator.generatePayments(1);
        entityService.saveEntityModel(payment);
        entityService.lockEntityModel(payment);
    }

    @AfterEach
    void deleteEntitiesAndModels() throws Exception {
        entityService.deleteAllEntitiesByModel("expense_report");
        entityService.deleteAllEntitiesByModel("employee");
        entityService.deleteAllEntitiesByModel("payment");
    }

    @AfterAll
    void tearDownAfterClass() throws Exception {
        entityService.deleteAllEntitiesByModel("expense_report");
        entityService.deleteAllEntitiesByModel("employee");
        entityService.deleteAllEntitiesByModel("payment");

        entityService.deleteEntityModel("expense_report");
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
        var reports = entityGenerator.generateExpenseReports(5, true);
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
        var report = entityGenerator.generateExpenseReports(1, true);
        HttpResponse response1 = entityService.saveEntities(report);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var reportFromDb = entityService.getAllEntitiesAsObjects("expense_report", "1");
        assertThat(reportFromDb).hasSize(1);
        var savedReportId = reportFromDb.get(0).getId();

        var stateBeforeTransition = entityService.getCurrentState(savedReportId);

        HttpResponse response2 = entityService.launchTransition(savedReportId, "SUBMIT");
        int statusCode2 = response2.getStatusLine().getStatusCode();
        var statusAfterTransition = entityService.getCurrentState(savedReportId);

        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);
        assertThat(stateBeforeTransition).isNotEqualTo(statusAfterTransition);
    }

//    @Test
//    public void updateEntityTest() throws Exception {
//        var report = entityGenerator.generateExpenseReports(1, true).get(0);
//        entityService.saveSingleEntity(report);
//
//        var entitiesFromDb = entityService.getAllEntitiesAsObjects("expense_report", "1");
//        assertThat(entitiesFromDb).hasSize(1);
//        ExpenseReport reportFromDb = (ExpenseReport) entitiesFromDb.get(0);
//        var reportFromDbId = reportFromDb.getId();
//
//        ExpenseReport updatedReport = entityGenerator.generateExpenseReports(1, true).get(0);
//        updatedReport.setId(reportFromDbId);
//        BigDecimal updatedAmountPayable = new BigDecimal("20.60");
//        updatedReport.setAmountPayable(updatedAmountPayable);
//        var expenses = updatedReport.getExpenseList();
//        expenses.remove(0);
//        updatedReport.setExpenseList(expenses);
//
//        HttpResponse responseUpdateEntity = entityService.updateEntity(updatedReport, "UPDATE");
//        assertThat(responseUpdateEntity.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
//
//        ExpenseReport updatedReportFromDb = entityService.getByIdAsObject(reportFromDbId);
//
//        assertThat(updatedReportFromDb).isNotEqualTo(reportFromDb);
//        assertThat(updatedReportFromDb.getAmountPayable()).isEqualTo(updatedAmountPayable);
//        assertThat(updatedReportFromDb.getExpenseList()).isEqualTo(expenses);
//    }

    @Test
    public void deleteAllTest() throws Exception {
        var employeeList = entityGenerator.generateEmployees(1);
        HttpResponse response1 = entityService.saveEntities(employeeList);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        HttpResponse response2 = entityService.deleteAllEntitiesByModel(employeeList);

        assertThat(response2.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void deleteByIdTest() throws Exception {

        var nEmployees = 1;
        var nReports = 1;

        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        var reports = entityGenerator.generateExpenseReports(nReports);
        entityService.saveEntities(reports);

        var reportFromDb = entityService.getAllEntitiesAsObjects("expense_report", "1");
        assertThat(reportFromDb).hasSize(1);
        var savedReportId = reportFromDb.get(0).getId();

        HttpResponse response1 = entityService.deleteEntityById(savedReportId.toString());
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void searchResultTest() throws Exception {
        var employees = entityGenerator.generateEmployees(1);
        entityService.saveEntities(employees);

        var nReports = 5;
        var reports = entityGenerator.generateExpenseReports(nReports);
        var report1 = reports.get(0);
        report1.setDestination("FirstCity");
        var report2 = reports.get(1);
        report2.setDestination("SecondCity");

        entityService.saveEntities(reports);

        var conditionRequest = new SearchConditionRequest();
        conditionRequest.setType("group");
        conditionRequest.setOperator("OR");
        conditionRequest.setConditions(List.of(
                new Condition("simple", "$.destination", "EQUALS", "FirstCity"),
                new Condition("simple", "$.destination", "EQUALS", "SecondCity")
        ));
        var snapshotId = entityService.runSearchAndGetSnapshotId("expense_report", "1", conditionRequest);

        assertThat(entityService.isSearchSuccessful(snapshotId)).isTrue();

        String json = entityService.getSearchResultAsJson(snapshotId);
        var entities = (List<ExpenseReport>) jsonToEntityListParser.parseResponse(json, ExpenseReport.class);
        assertThat(entities.size()).isEqualTo(2);
        Set<String> cities = entities.stream()
                .map(ExpenseReport::getDestination)
                .collect(Collectors.toSet());
        assertThat(cities).contains("FirstCity", "SecondCity");
    }

    @Test
    public void searchTest() throws Exception {
        var searchPageSize = 50;
        var searchPageNumber = 0;
        var nReports = 50;
        var reports = entityGenerator.generateExpenseReports(nReports, true);

        entityService.saveEntities(reports);

        var conditionRequest = new SearchConditionRequest();
        conditionRequest.setType("group");
        conditionRequest.setOperator("AND");
        conditionRequest.setConditions(List.of(
                new Condition("simple", "$.destination", "NOT_EQUAL", "ImpossibleCity")
        ));
        var snapshotId = entityService.runSearchAndGetSnapshotId("expense_report", "1", conditionRequest);

        assertThat(entityService.isSearchSuccessful(snapshotId)).isTrue();

        String json = entityService.getSearchResultAsJson(snapshotId, searchPageSize, searchPageNumber);
        var entities = (List<ExpenseReport>) jsonToEntityListParser.parseResponse(json, ExpenseReport.class);
        assertThat(entities.size()).isEqualTo(nReports);
    }

    @Test
    public void getAllEntitiesByModelTest() throws Exception {
        var nEmployees = 3;
        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        var employeesFromDb = entityService.getAllEntitiesAsObjects("employee", "1");
        assertThat(employeesFromDb.size()).isEqualTo(nEmployees);

        var nReports = 5;
        var reports = entityGenerator.generateExpenseReports(nReports);
        entityService.saveEntities(reports);
        var reportsFromDb = entityService.getAllEntitiesAsObjects("expense_report", "1");
        assertThat(reportsFromDb.size()).isEqualTo(nReports);
    }

    @Test
    public void getByIdAsObjectTest() throws Exception {
        var nEmployees = 1;
        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);

        var employeesFromDb = entityService.getAllEntitiesAsObjects("employee", "1");
        assertThat(employeesFromDb.size()).isEqualTo(nEmployees);

        var employeeId = employeesFromDb.get(0).getId();
        var employeeFromDb = entityService.getByIdAsObject(employeeId);
        assertThat(employeeFromDb).isEqualTo(employeesFromDb.get(0));


        var nReports = 1;
        var reports = entityGenerator.generateExpenseReports(nReports);
        entityService.saveEntities(reports);
        var reportsFromDb = entityService.getAllEntitiesAsObjects("expense_report", "1");
        assertThat(reportsFromDb.size()).isEqualTo(nReports);

        var reportId = reportsFromDb.get(0).getId();
        var reportFromDb = entityService.getByIdAsObject(reportId);

        assertThat(reportFromDb).isEqualTo(reportsFromDb.get(0));
    }

    @Test
    public void getByIdAsJson() throws Exception {
        var nEmployees = 1;
        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);

        var employeesFromDb = entityService.getAllEntitiesAsObjects("employee", "1");
        assertThat(employeesFromDb.size()).isEqualTo(nEmployees);

        var employeeId = employeesFromDb.get(0).getId();
        String employeeFromDb = entityService.getByIdAsJson(employeeId);

    }

    @Test
    public void workflowTest() throws Exception {
        var nEmployees = 1;
        var nReports = 5;
        var nTransitions = 20;

        var employees = entityGenerator.generateEmployees(nEmployees);
        entityService.saveEntities(employees);
        //        ExpenseReport is created by an existing employee
        var reports = entityGenerator.generateExpenseReports(nReports);
        entityService.saveEntities(reports);

        var reportsFromDb = entityService.getAllEntitiesAsObjects("expense_report", "1");
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
//                            ExpenseReport report = entityService.getByIdAsObject(randomReportId);
//                            report.setDestination("UpdatedCity");
//                            entityService.updateEntity(report, randomTransition);
                            break;
                        case "POST_PAYMENT":
//                        should be launched by an EP - payment transition "ACCEPT_BY_BANK"
                            break;
                        case "CREATE_PAYMENT":
//                        should be launched automatically
                            break;
                        case "PROCEED_WITHOUT_PAYMENT":
//                        should be launched automatically
                            break;
                        case "DELETE":
//                        for test purposes
                            break;
                        case "REJECT_BY_ACCOUNTING":
//                        for test purposes
                            break;
                        case "REJECT_BY_MANAGER":
//                        for test purposes
                            break;
                        default:
                            entityService.launchTransition(randomReportId, randomTransition);
                            break;
                    }
                }
// added delay to let externalized processor run and finish before next transition cycle
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted: " + e.getMessage());
            }
        }
    }
}