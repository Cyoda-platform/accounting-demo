package com.example.accounting_demo.auxiliary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmbeddedWrapper {
    @JsonProperty("_embedded")
    private ObjectNodesWrapper embedded;

    @Getter
    @Setter
    public static class ObjectNodesWrapper {
        @JsonProperty("objectNodes")
        private List<TreeWrapper> objectNodes;
    }

    @Getter
    @Setter
    public static class TreeWrapper {
        private String id;
        private Object tree;
    }
}
