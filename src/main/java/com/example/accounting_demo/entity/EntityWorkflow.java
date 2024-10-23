package com.example.accounting_demo.entity;

import com.example.accounting_demo.common.ai.AIAssistantService;
import com.example.accounting_demo.common.ingestion.DataIngestionService;

import com.example.accounting_demo.entity.expense_report_request.ExpenseReportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyoda.cloud.api.event.BaseEvent;
import org.cyoda.cloud.api.event.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.EntityProcessorCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Component
public class EntityWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(EntityWorkflow.class);
    public static final String EXPENSE_REPORT_SCHEMA_ID = "46b25fa0-90da-11ef-b47d-a6dbe11f1714";
    public static final String DS_1_ID = "86c97f70-90d9-11ef-b47d-a6dbe11f1714";
    public static final String DS_2_ID = "aaba3e10-90d9-11ef-b47d-a6dbe11f1714";

    private final ObjectMapper objectMapper;
    private final DataIngestionService dataIngestionService;
    private final AIAssistantService aiAssistantService;

    public EntityWorkflow(ObjectMapper om, DataIngestionService dataIngestionService, AIAssistantService aiAssistantService) {
        this.objectMapper = om;
        this.dataIngestionService = dataIngestionService;
        this.aiAssistantService = aiAssistantService;
    }


    public BaseEvent calculate(EntityProcessorCalculationRequest request) throws IOException, InterruptedException {
        EntityProcessorCalculationResponse response = new EntityProcessorCalculationResponse();

        response.setOwner(request.getOwner());
        response.setRequestId(request.getRequestId());
        response.setEntityId(request.getEntityId());

        switch (request.getProcessorName()) {
            case "GenerateReport":
                response.setPayload(request.getPayload());
                String question = "Generate a report to get total expenses by employee, by category, by project. Return only report without comments.";
                String report = aiAssistantService.chat(EXPENSE_REPORT_SCHEMA_ID, question);
                break;
            case "IngestDataFromSystem1":
                String requestId = dataIngestionService.ingestData(DS_1_ID, "Fetch Accounting Data", new HashMap<>());
                ExpenseReportRequest expenseReportRequest = objectMapper.treeToValue(request.getPayload().getData(), ExpenseReportRequest.class);
                expenseReportRequest.setRequestIds(new ArrayList<>(List.of(UUID.fromString(requestId.replaceAll("\"", "")))));
                request.getPayload().setData(objectMapper.valueToTree(expenseReportRequest));
                response.setPayload(request.getPayload());
                break;
            case "IngestDataFromSystem2":
                requestId = dataIngestionService.ingestData(DS_2_ID, "Fetch Accounting Data", new HashMap<>());
                response.setPayload(request.getPayload());
                expenseReportRequest = objectMapper.treeToValue(request.getPayload().getData(), ExpenseReportRequest.class);
                expenseReportRequest.getRequestIds().add(UUID.fromString(requestId.replaceAll("\"", "")));
                request.getPayload().setData(objectMapper.valueToTree(expenseReportRequest));
                response.setPayload(request.getPayload());
                break;
            case "SendReport":
                logger.info("Sending report");
                response.setPayload(request.getPayload());
                break;
            case "VerifyIngestionForSystem1":
                expenseReportRequest = objectMapper.treeToValue(request.getPayload().getData(), ExpenseReportRequest.class);
                for (UUID reqId : expenseReportRequest.getRequestIds()){
                    String resp = dataIngestionService.getDataSourceResult(String.valueOf(reqId));
                    //todo validate resp and add finished request
                }
                response.setPayload(request.getPayload());
                break;
            default:
                logger.info("No corresponding processor found");
                break;
        }

        return response;
    }

}
/*

SELECT employee_name, SUM(amount) as total_expenses
FROM employee_expense.employee_expense
GROUP BY employee_name


SELECT expense_category, SUM(amount) AS total_expenses FROM employee_expense.employee_expense GROUP BY expense_category

SELECT project_name, SUM(amount) AS total_expenses FROM employee_expense.employee_expense GROUP BY project_name

 */