package com.springsearchengine.dto;

import lombok.Data;

@Data
public class SearchPage {
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

    public SearchPage(String uri, String title, String snippet, double relevance) {
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}