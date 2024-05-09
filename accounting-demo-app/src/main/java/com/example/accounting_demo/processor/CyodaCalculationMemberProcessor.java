package com.example.accounting_demo.processor;

import com.example.accounting_demo.model.BusinessTravelReport;
import com.example.accounting_demo.model.Payment;
import com.example.accounting_demo.repository.BusinessTravelReportRepository;
import com.example.accounting_demo.service.EntityPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyoda.cloud.api.event.BaseEvent;
import org.cyoda.cloud.api.event.DataPayload;
import org.cyoda.cloud.api.event.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.EntityProcessorCalculationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class CyodaCalculationMemberProcessor {

    @Autowired
    BusinessTravelReportRepository reportRepository;

    @Autowired
    ObjectMapper mapper;
    @Autowired
    private EntityPublisher entityPublisher;

    public BaseEvent calculate(EntityProcessorCalculationRequest request) throws IOException {
        EntityProcessorCalculationResponse response = new EntityProcessorCalculationResponse();

        response.setOwner(request.getOwner());
        response.setRequestId(request.getRequestId());
        response.setEntityId(request.getEntityId());

        DataPayload payload = new DataPayload();
        payload.setType("TreeNode");
        payload.setData(request.getPayload() != null ? request.getPayload().getData() : null);

        response.setPayload(payload);

        switch (request.getProcessorName()) {
            case "sendNewEntityToClient":
                saveEntityToRepo(request);
                break;
            case "notifyApprover":
                notifyApprover(request);
                break;
            case "schedulePayment":
                schedulePayment(request);
                break;

            default:
                System.out.println("No corresponding processor found");
                break;
        }

        return response;
    }
    //needed for tests; saves entities from saas to the local db; can be replaced with saving a list of ids, received from saas after entity creation
    public void saveEntityToRepo(EntityProcessorCalculationRequest request) throws JsonProcessingException {
        var data = request.getPayload().getData();
        String dataJson = mapper.writeValueAsString(data);

        try {
            BusinessTravelReport report = mapper.readValue(dataJson, BusinessTravelReport.class);
            report.setId(UUID.fromString(request.getEntityId()));
            reportRepository.save(report);
            System.out.println("Saved reports: " + report);
        } catch (JsonMappingException e) {
            System.err.println("Error saving entity: JSON does not match the class fields.");
            e.printStackTrace();
        }
    }
    //imitates email notification
    public void notifyApprover(EntityProcessorCalculationRequest request) {
        System.out.println("Report with id: " + request.getEntityId() + " SUBMITTED");

    }
    //creates and saves new payment entity
    public void schedulePayment(EntityProcessorCalculationRequest request) throws IOException {
        var data = request.getPayload().getData();
        String dataJson = mapper.writeValueAsString(data);

        BusinessTravelReport report = mapper.readValue(dataJson, BusinessTravelReport.class);
        String totalAmount = report.getTotalAmount();
        
        Payment payment = new Payment();
        payment.setBtReportId(UUID.fromString(request.getEntityId()));
        payment.setAmount(totalAmount);

        entityPublisher.saveEntities(List.of(payment));
    }

}
