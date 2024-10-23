package com.example.accounting_demo.service;

import com.example.accounting_demo.common.repository.CyodaHttpRepositoryHelper;
import com.example.accounting_demo.common.service.EntityServiceImpl;
import com.example.accounting_demo.common.util.JsonToEntityListParser;
import com.example.accounting_demo.entity.expense_report_request.ExpenseReportRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityServiceTest {


    @Autowired
    private EntityServiceImpl entityService;

    @Autowired
    private JsonToEntityListParser jsonToEntityListParser;

    @BeforeAll
    void setUpBeforeClass() throws Exception {
    }

    @AfterEach
    void deleteEntitiesAndModels() throws Exception {
    }

    @AfterAll
    void tearDownAfterClass() throws Exception {
    }


    @Test
    public void saveExpenseReportRequestTest() throws Exception {
        Thread.sleep(10000);
        ExpenseReportRequest reportRequest = new ExpenseReportRequest();
        reportRequest.setExpenseReportRequestId(UUID.randomUUID());
        reportRequest.setDate(String.valueOf(LocalDate.now()));
        //todo
        reportRequest.setReportId(UUID.randomUUID());
        reportRequest.setRequestIds(Collections.singletonList(UUID.randomUUID()));
        reportRequest.setFinishedRequestIds(Collections.singletonList(UUID.randomUUID()));
        entityService.addItem(reportRequest);
        Thread.sleep(100000);
    }

}