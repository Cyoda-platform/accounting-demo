package com.example.accounting_demo.entity.expense_report_request;

import com.example.accounting_demo.common.repository.CyodaEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ExpenseReportRequest extends CyodaEntity {

    private UUID expenseReportRequestId;
    private String date;
    private List<UUID> requestIds;
    private List<UUID> finishedRequestIds;
    private UUID reportId;

}
