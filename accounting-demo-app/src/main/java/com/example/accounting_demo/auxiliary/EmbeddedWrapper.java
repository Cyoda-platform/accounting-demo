package com.example.accounting_demo.auxiliary;

import com.example.accounting_demo.model.ExpenseReportNested;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EmbeddedWrapper {
    @JsonProperty("_embedded")
    private ObjectNodesWrapper embedded;

    @Getter
    @Setter
    public static class ObjectNodesWrapper {
        @JsonProperty("objectNodes")
        private List<EntityWrapper> objectNodes;
    }

    @Getter
    @Setter
    public static class EntityWrapper {
        private String id;
        private Object entity;
    }
}
