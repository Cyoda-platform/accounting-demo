package com.example.accounting_demo.common.service;

import com.example.accounting_demo.common.repository.BaseEntity;
import com.example.accounting_demo.common.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


@Service
public class EntityServiceImpl implements EntityService<BaseEntity> {

    private final CrudRepository<BaseEntity, UUID> repository;

    public EntityServiceImpl(CrudRepository<? extends BaseEntity, UUID> repository) {
        this.repository = (CrudRepository<BaseEntity, UUID>) repository;
    }

    @Override
    public BaseEntity getItem(UUID id) {
        return null;
    }

    @Override
    public List<BaseEntity> getItems() {
        return List.of();
    }

    @Override
    public List<BaseEntity> getSingleItemByCondition(String condition) {
        return List.of();
    }

    @Override
    public List<BaseEntity> getItemsByCondition(String condition) {
        return List.of();
    }

    @Override
    public BaseEntity addItem(BaseEntity entity) {
        return repository.save(entity);
    }

    @Override
    public BaseEntity updateItem(String id, BaseEntity entity) {
        return null;
    }
}



