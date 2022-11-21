package com.springsearchengine.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class SearchRequest {

    private String query;
    private String site;
    private int offset;
    private int limit;
}
