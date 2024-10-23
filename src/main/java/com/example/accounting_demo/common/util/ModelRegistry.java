package com.example.accounting_demo.common.util;

import com.example.accounting_demo.common.repository.BaseEntity;
import com.example.accounting_demo.entity.expense_report.ExpenseReport;
import com.example.accounting_demo.entity.expense_report_request.ExpenseReportRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ModelRegistry {
    private static final Map<String, Class<? extends BaseEntity>> modelToClassMap = new HashMap<>();
    private static final Map<Class<? extends BaseEntity>, String> classToModelMap = new HashMap<>();

    static {
        modelToClassMap.put("expense_report", ExpenseReport.class);
        modelToClassMap.put("expense_report_request", ExpenseReportRequest.class);
        classToModelMap.put(ExpenseReport.class, "expense_report");
        classToModelMap.put(ExpenseReportRequest.class, "expense_report_request");
    }

    public static Class<? extends BaseEntity> getClassByModel(String model) {
        return modelToClassMap.get(model);
    }

    public static String getModelByClass(Class<? extends BaseEntity> clazz) {
        return classToModelMap.get(clazz);
    }
}
