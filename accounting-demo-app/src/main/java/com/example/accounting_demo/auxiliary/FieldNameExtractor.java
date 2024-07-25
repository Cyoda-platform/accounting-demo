package com.example.accounting_demo.auxiliary;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

//temporary workaround to get all entities by model (gets an actual field name to be used in setting an impossible search condition)
@Component
public class FieldNameExtractor {
    public static Set<String> getFieldNames(Class<?> clazz) {
        Set<String> fieldNames = new HashSet<>();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }
}
