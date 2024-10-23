package com.example.accounting_demo.common.service;

import com.example.accounting_demo.common.repository.BaseEntity;

import java.util.List;
import java.util.UUID;

public interface EntityService<T extends BaseEntity> {

    T getItem(UUID id);

    List<T> getItems();

    List<T> getSingleItemByCondition(String condition);

    List<T> getItemsByCondition(String condition);

    T addItem(T entity);

    T updateItem(String id, T entity);
}
