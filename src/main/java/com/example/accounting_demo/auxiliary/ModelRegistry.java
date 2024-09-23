package com.example.accounting_demo.auxiliary;

import com.example.accounting_demo.model.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ModelRegistry {
    private static final Map<String, Class<? extends BaseEntity>> modelToClassMap = new HashMap<>();
    private static final Map<Class<? extends BaseEntity>, String> classToModelMap = new HashMap<>();

    static {
        modelToClassMap.put("payment", Payment.class);
        modelToClassMap.put("expense_report", ExpenseReport.class);
        modelToClassMap.put("employee", Employee.class);

        classToModelMap.put(Payment.class, "payment");
        classToModelMap.put(ExpenseReport.class, "expense_report");
        classToModelMap.put(Employee.class, "employee");
    }

    public static Class<? extends BaseEntity> getClassByModel(String model) {
        return modelToClassMap.get(model);
    }

    public static String getModelByClass(Class<? extends BaseEntity> clazz) {
        return classToModelMap.get(clazz);
    }
}
