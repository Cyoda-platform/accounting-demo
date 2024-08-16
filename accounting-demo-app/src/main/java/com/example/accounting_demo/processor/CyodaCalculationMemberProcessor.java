package com.example.accounting_demo.processor;

import com.example.accounting_demo.model.Expense;
import com.example.accounting_demo.model.ExpenseReport;
import com.example.accounting_demo.model.Payment;
import com.example.accounting_demo.service.EntityService;
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

    private final ObjectMapper mapper;
    private final EntityService entityService;

    public CyodaCalculationMemberProcessor(ObjectMapper mapper, EntityService entityService) {
        this.mapper = mapper;
        this.entityService = entityService;
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
        String dataJson = mapper.writeValueAsString(data);
        Payment payment = mapper.readValue(dataJson, Payment.class);
        var expenseReportId = payment.getExpenseReportId();
        entityService.launchTransition(expenseReportId, "POST_PAYMENT");
    }

    private Object calculateAmountPayable(EntityProcessorCalculationRequest request) throws IOException {
        var reportId = UUID.fromString(request.getEntityId());
        var data = request.getPayload().getData();
        String dataJson = mapper.writeValueAsString(data);
        ExpenseReport report = mapper.readValue(dataJson, ExpenseReport.class);
        report.setId(reportId);

        List<Expense> expensesList = report.getExpenseList();
        BigDecimal multiplier = new BigDecimal("0.5");
        BigDecimal calculatedAmount = expensesList.stream()
                .map(expense -> expense.getDescription().equals("meals") ? expense.getAmount().multiply(multiplier) : expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        report.setAmountPayable(calculatedAmount);

        return report;
    }

    private void sendToBank(EntityProcessorCalculationRequest request) throws IOException {
        boolean isEnoughFunds = new Random().nextBoolean();
        var paymentId = UUID.fromString(request.getEntityId());

        logger.info((entityService.getCurrentState(paymentId) + " is state of payment with id: " + paymentId));

        if (isEnoughFunds) {
            entityService.launchTransition(UUID.fromString(request.getEntityId()), "ACCEPT_BY_BANK");
        } else {
            entityService.launchTransition(UUID.fromString(request.getEntityId()), "REJECT_BY_BANK");
        }

        logger.info((entityService.getCurrentState(paymentId) + " is state of payment with id: " + paymentId));
    }

    //imitates email notification
    public void sendNotification(EntityProcessorCalculationRequest request) throws IOException {
        var id = UUID.fromString(request.getEntityId());
        var state = entityService.getCurrentState(id);
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
        String dataJson = mapper.writeValueAsString(data);

        ExpenseReport report = mapper.readValue(dataJson, ExpenseReport.class);
        BigDecimal amountPayable = report.getAmountPayable();

        Payment payment = new Payment();
        payment.setExpenseReportId(UUID.fromString(request.getEntityId()));
        payment.setAmount(amountPayable);

        entityService.saveSingleEntity(payment);
    }
}
