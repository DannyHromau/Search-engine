package com.springsearchengine.dto;

import lombok.Data;

@Data
public class SearchPageResponse {
    private String uri;
    private String name;
    private String title;
    private String snippet;
    private double relevance;

    public SearchPageResponse(String uri, String name, String title, String snippet, double relevance) {
        this.uri = uri;
        this.name = name;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}