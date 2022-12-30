package com.springsearchengine.service;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.model.entity.Field;
import com.springsearchengine.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(
        value = "newDB",
        havingValue = "true")
public class FieldSaver {
    @Autowired
    private FieldConfig fieldConfig;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void runAfterStartup() {
        jdbcTemplate.update("DELETE FROM site");
        jdbcTemplate.update("DELETE FROM lemma");
        jdbcTemplate.update("DELETE FROM field");
        jdbcTemplate.update("DELETE FROM page_data");
        jdbcTemplate.update("DELETE FROM page_index");
        for (FieldConfig.FieldList fieldList : fieldConfig.getFieldList()) {
            Field field = new Field(fieldList.getName(), fieldList.getSelector(), fieldList.getWeight());
            fieldRepository.save(field);
        }
    }

}

