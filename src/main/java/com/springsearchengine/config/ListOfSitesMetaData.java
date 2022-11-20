package com.springsearchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sites")
public class ListOfSitesMetaData {
    private List<SiteList> siteList;

    @Data
    public static class SiteList {

        private String url;
        private String name;
    }
}
