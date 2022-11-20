package com.springsearchengine.dto;

import lombok.Data;

@Data
public class SearchRequest {

    private String query;
    private String url;
    private int offset;
    private int limit;
}
