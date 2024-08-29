package com.example.accounting_demo.processor;

import com.example.accounting_demo.model.Expense;
import com.example.accounting_demo.model.ExpenseReport;
import com.example.accounting_demo.model.Payment;
import com.example.accounting_demo.service.EntityServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyoda.cloud.api.event.BaseEvent;
import org.cyoda.cloud.api.event.DataPayload;
import org.cyoda.cloud.api.event.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.EntityProcessorCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class CyodaCalculationMemberProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    private final ObjectMapper om;
    private final EntityServiceImpl entityServiceImpl;

    public CyodaCalculationMemberProcessor(ObjectMapper om, EntityServiceImpl entityServiceImpl) {
        this.om = om;
        this.entityServiceImpl = entityServiceImpl;
    }


    public BaseEvent calculate(EntityProcessorCalculationRequest request) throws IOException, InterruptedException {
        EntityProcessorCalculationResponse response = new EntityProcessorCalculationResponse();

        response.setOwner(request.getOwner());
        response.setRequestId(request.getRequestId());
        response.setEntityId(request.getEntityId());

        switch (request.getProcessorName()) {
            case "sendNotification":
                sendNotification(request);
                break;
            case "createPayment":
                createPayment(request);
                break;
            case "isEnoughFunds":
                sendToBank(request);
                break;
            case "postPayment":
                postPayment(request);
                break;
            case "calculateAmountPayable":
                DataPayload payload = new DataPayload();
                payload.setType("TREE");
                payload.setData(calculateAmountPayable(request));
                response.setPayload(payload);
                break;
            default:
                logger.info("No corresponding processor found");
                break;
        }

        return response;
    }

    private void postPayment(EntityProcessorCalculationRequest request) throws IOException {
        var data = request.getPayload().getData();
        String dataJson = om.writeValueAsString(data);
        Payment payment = om.readValue(dataJson, Payment.class);
        var expenseReportId = payment.getExpenseReportId();
        entityServiceImpl.launchTransition(expenseReportId, "POST_PAYMENT");
    }

    private JsonNode calculateAmountPayable(EntityProcessorCalculationRequest request) throws IOException {
        var reportId = UUID.fromString(request.getEntityId());
        var data = request.getPayload().getData();
        String dataJson = om.writeValueAsString(data);
        ExpenseReport report = om.readValue(dataJson, ExpenseReport.class);
        report.setId(reportId);

        List<Expense> expensesList = report.getExpenseList();
        BigDecimal multiplier = new BigDecimal("0.5");
        BigDecimal calculatedAmount = expensesList.stream()
                .map(expense -> expense.getDescription().equals("meals") ? expense.getAmount().multiply(multiplier) : expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        calculatedAmount = calculatedAmount.subtract(report.getAdvancePayment());
        report.setAmountPayable(calculatedAmount);

        return om.valueToTree(report);
    }

    private void sendToBank(EntityProcessorCalculationRequest request) throws IOException {
        boolean isEnoughFunds = new Random().nextBoolean();
        var paymentId = UUID.fromString(request.getEntityId());

        logger.info((entityServiceImpl.getCurrentState(paymentId) + " is state of payment with id: " + paymentId));

        if (isEnoughFunds) {
            entityServiceImpl.launchTransition(UUID.fromString(request.getEntityId()), "ACCEPT_BY_BANK");
        } else {
            entityServiceImpl.launchTransition(UUID.fromString(request.getEntityId()), "REJECT_BY_BANK");
        }

        logger.info((entityServiceImpl.getCurrentState(paymentId) + " is state of payment with id: " + paymentId));
    }

    //imitates email notification
    public void sendNotification(EntityProcessorCalculationRequest request) throws IOException {
        var id = UUID.fromString(request.getEntityId());
        var state = entityServiceImpl.getCurrentState(id);
        var message = "";
        switch (state) {
            case "SUBMITTED":
                message = " is pending accounting approval";
                break;
            case "APPROVED_BY_ACCOUNTING":
                message = " is pending manager's approval";
                break;
            case "APPROVED_BY_MANAGER":
                message = " is pending payment";
                break;
            default:
                break;
        }
        logger.info("E-MAIL RECEIVED: ER with id: {}{}", request.getEntityId(), message);
    }

    public void createPayment(EntityProcessorCalculationRequest request) throws IOException {
        var data = request.getPayload().getData();
        String dataJson = om.writeValueAsString(data);

        ExpenseReport report = om.readValue(dataJson, ExpenseReport.class);
        BigDecimal amountPayable = report.getAmountPayable();

        Payment payment = new Payment();
        payment.setExpenseReportId(UUID.fromString(request.getEntityId()));
        payment.setAmount(amountPayable);

        entityServiceImpl.saveSingleEntity(payment);
    }
}
