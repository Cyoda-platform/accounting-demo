package com.example.accounting_demo.processor;

import com.example.accounting_demo.model.ExpenseReport;
import com.example.accounting_demo.model.Payment;
import com.example.accounting_demo.service.EntityIdLists;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class CyodaCalculationMemberProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    private final ObjectMapper mapper;
    private final EntityService entityService;
    final EntityIdLists entityIdLists;

    public CyodaCalculationMemberProcessor(ObjectMapper mapper, EntityService entityService, EntityIdLists entityIdLists) {
        this.mapper = mapper;
        this.entityService = entityService;
        this.entityIdLists = entityIdLists;
    }


    public BaseEvent calculate(EntityProcessorCalculationRequest request) throws IOException, InterruptedException {
        EntityProcessorCalculationResponse response = new EntityProcessorCalculationResponse();

        response.setOwner(request.getOwner());
        response.setRequestId(request.getRequestId());
        response.setEntityId(request.getEntityId());

        DataPayload payload = new DataPayload();
        payload.setType("TreeNode");
        payload.setData(request.getPayload() != null ? request.getPayload().getData() : null);

        response.setPayload(payload);

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
            case "saveRootId":
                saveRootId(request);
                break;

            default:
                logger.info("No corresponding processor found");
                break;
        }

        return response;
    }

    private void saveRootId(EntityProcessorCalculationRequest request) {
        var id = UUID.fromString(request.getEntityId());
        entityIdLists.addToExpenseReportIdList(List.of(id));
        logger.info("IdList updated with id: {}", id);
    }

    private void postPayment(EntityProcessorCalculationRequest request) throws IOException {
        var expenseReportId = entityService.getValue(UUID.fromString(request.getEntityId()), "values@org#cyoda#entity#model#ValueMaps.timeuuids.[.expenseReportId]");
        entityService.launchTransition(UUID.fromString(expenseReportId), "POST_PAYMENT");
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

    //creates and saves new payment entity
    public void createPayment(EntityProcessorCalculationRequest request) throws IOException {
        var data = request.getPayload().getData();
        String dataJson = mapper.writeValueAsString(data);

        ExpenseReport report = mapper.readValue(dataJson, ExpenseReport.class);
        String totalAmount = report.getTotalAmount();

        Payment payment = new Payment();
        payment.setExpenseReportId(UUID.fromString(request.getEntityId()));
        payment.setAmount(totalAmount);

        entityService.saveEntities(List.of(payment));
    }

}
