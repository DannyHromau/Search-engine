package com.springsearchengine.dao;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.model.entity.Site;

public class ManagerThread implements Runnable {

    private SearchEngineManager searchEngineManager;
    private Site site;
    private FieldConfig fieldConfig;

    public ManagerThread(SearchEngineManager searchEngineManager, Site site, FieldConfig fieldConfig) {
        this.searchEngineManager = searchEngineManager;
        this.site = site;
        this.fieldConfig = fieldConfig;
    }

    @Override
    public void run() {
        searchEngineManager.startIndexing(site, fieldConfig);

    }
}
