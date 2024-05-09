package com.example.accounting_demo.service;

import com.example.accounting_demo.auxiliary.EntityGenerator;
import com.example.accounting_demo.repository.BusinessTravelReportRepository;
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
    private BusinessTravelReportRepository reportRepository;

    @Test
    public void saveEntitySchemaTest() throws Exception {
        var report = entityGenerator.generateReport();
        HttpResponse response1 = entityPublisher.saveEntitySchema(report);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var payment = entityGenerator.generatePayment();
        HttpResponse response2 = entityPublisher.saveEntitySchema(payment);
        int statusCode2 = response2.getStatusLine().getStatusCode();
        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void lockEntitySchemaTest() throws Exception {
        var report = entityGenerator.generateReport();
        HttpResponse response1 = entityPublisher.lockEntitySchema(report);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var payment = entityGenerator.generatePayment();
        HttpResponse response2 = entityPublisher.lockEntitySchema(payment);
        int statusCode2 = response2.getStatusLine().getStatusCode();
        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);
    }

    //also saves an extra parent entity, need to add check at saveEntityToRepo()
    @Test
    public void saveBtReportListTest() throws Exception {
        var reports = entityGenerator.generateReport(2);
        HttpResponse response = entityPublisher.saveEntities(reports);
        int statusCode = response.getStatusLine().getStatusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void savePaymentListTest() throws Exception {
        var payment = entityGenerator.generatePayment();
        HttpResponse response = entityPublisher.saveEntities(payment);
        int statusCode = response.getStatusLine().getStatusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void launchTransitionTest() throws Exception {
        var report = entityGenerator.generateReport();
        HttpResponse response1 = entityPublisher.saveEntities(report);
        int statusCode1 = response1.getStatusLine().getStatusCode();
        assertThat(statusCode1).isEqualTo(HttpStatus.SC_OK);

        var savedReport = reportRepository.findAll().get(0);
        var statusBeforeTransition = entityService.getCurrentState(savedReport.getId());

        HttpResponse response2 = entityService.launchTransition(savedReport.getId(), "SUBMIT");
        int statusCode2 = response2.getStatusLine().getStatusCode();
        var statusAfterTransition = entityService.getCurrentState(savedReport.getId());

        assertThat(statusCode2).isEqualTo(HttpStatus.SC_OK);
        assertThat(statusBeforeTransition).isNotEqualTo(statusAfterTransition);

    }

    @Test
    public void getValueTest() throws Exception {
        var report = entityGenerator.generateReport();
        HttpResponse response = entityPublisher.saveEntities(report);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        var savedReport = reportRepository.findAll().get(0);
        var valueLocal = savedReport.getEmployeeName();
        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.employeeName]";

        var value = entityService.getValue(savedReport.getId(), columnPath);

        assertThat(value).isEqualTo(valueLocal);
    }

    @Test
    public void updateValueTest() throws Exception {
        var report = entityGenerator.generateReport();
        HttpResponse response1 = entityPublisher.saveEntities(report);
        assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        String columnPath = "values@org#cyoda#entity#model#ValueMaps.strings.[.employeeName]";

        var savedReport = reportRepository.findAll().get(0);
        String updatedValue = "updatedName";

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.valueToTree(updatedValue);

        HttpResponse response2 = entityService.updateValue(savedReport.getId(), columnPath, jsonNode);

        var currentValue = entityService.getValue(savedReport.getId(), columnPath);

        assertThat(response2.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(currentValue).isEqualTo(updatedValue);
    }

}