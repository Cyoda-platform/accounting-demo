package com.example.accounting_demo.processor;

import com.example.accounting_demo.model.ExpenseReport;
import com.example.accounting_demo.model.Payment;
import com.example.accounting_demo.service.EntityPublisher;
import com.example.accounting_demo.service.EntityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyoda.cloud.api.event.BaseEvent;
import org.cyoda.cloud.api.event.DataPayload;
import org.cyoda.cloud.api.event.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.EntityProcessorCalculationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class CyodaCalculationMemberProcessor {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private EntityPublisher entityPublisher;

    @Autowired
    private EntityService entityService;

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
            case "notifyApprover":
                notifyApprover(request);
                break;
            case "createPayment":
                createPayment(request);
                break;
            case "sendToBank":
                sendToBank(request);
                break;
            case "postPayment":
                postPayment(request);
                break;

            default:
                System.out.println("No corresponding processor found");
                break;
        }

        return response;
    }

    private void postPayment(EntityProcessorCalculationRequest request) throws IOException {
        var travelReportId = entityService.getValue(UUID.fromString(request.getEntityId()), "values@org#cyoda#entity#model#ValueMaps.timeuuids.[.btReportId]");
        entityService.launchTransition(UUID.fromString(travelReportId), "POST_PAYMENT");
    }

    private void sendToBank(EntityProcessorCalculationRequest request) throws IOException, InterruptedException {
        boolean isEnoughFunds = new Random().nextBoolean();
        var paymentId = UUID.fromString(request.getEntityId());

        System.out.println((entityService.getCurrentState(paymentId) + " is state of payment with id: " + paymentId));

        if (isEnoughFunds) {
            entityService.launchTransition(UUID.fromString(request.getEntityId()), "ACCEPT_BY_BANK");
        } else {
            entityService.launchTransition(UUID.fromString(request.getEntityId()), "REJECT_BY_BANK");
        }

        System.out.println((entityService.getCurrentState(paymentId) + " is state of payment with id: " + paymentId));
    }

    //imitates email notification
    public void notifyApprover(EntityProcessorCalculationRequest request) {
        System.out.println("Report with id: " + request.getEntityId() + " SUBMITTED");

    }

    //creates and saves new payment entity
    public void createPayment(EntityProcessorCalculationRequest request) throws IOException {
        var data = request.getPayload().getData();
        String dataJson = mapper.writeValueAsString(data);

        ExpenseReport report = mapper.readValue(dataJson, ExpenseReport.class);
        String totalAmount = report.getTotalAmount();
        
        Payment payment = new Payment();
        payment.setBtReportId(UUID.fromString(request.getEntityId()));
        payment.setAmount(totalAmount);

        entityPublisher.saveEntities(List.of(payment));
    }

}
