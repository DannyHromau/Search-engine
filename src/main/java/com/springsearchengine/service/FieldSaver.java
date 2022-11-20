package com.springsearchengine.service;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.model.entity.Field;
import com.springsearchengine.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FieldSaver {
    @Autowired
    FieldConfig fieldConfig = new FieldConfig();
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private PageDataRepository pageDataRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Value("${newDB}")
    private boolean createNewDataInDB;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void runAfterStartup() {
        if (createNewDataInDB) {
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

}

