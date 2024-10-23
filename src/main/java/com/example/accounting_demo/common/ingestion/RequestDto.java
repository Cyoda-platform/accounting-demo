package com.example.accounting_demo.common.ingestion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RequestDto {

    @JsonProperty("data_source_operations")
    private List<DataSourceOperation> dataSourceOperations;

    @JsonProperty("datasource_id")
    private String dataSourceId;

    public RequestDto(List<DataSourceOperation> dataSourceOperations, String dataSourceId) {
        this.dataSourceOperations = dataSourceOperations;
        this.dataSourceId = dataSourceId;
    }

    public static class DataSourceOperation {
        @JsonProperty("operation")
        private String operation;

        @JsonProperty("request_fields")
        private Map<String, String> requestFields;

        public DataSourceOperation(String operation, Map<String, String> requestFields) {
            this.operation = operation;
            this.requestFields = requestFields;
        }

    }

}
