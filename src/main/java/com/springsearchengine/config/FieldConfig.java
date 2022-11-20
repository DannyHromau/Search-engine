package com.springsearchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "fields")
public class FieldConfig {
    private List<FieldList> fieldList;

    @Data
    public static class FieldList {
        private String name;
        private String selector;
        private Double weight;
    }
}
