package com.springsearchengine.dto.data;

import lombok.Data;

import java.util.List;

@Data
public class SearchPageData {

    private boolean result;
    private int count;
    private List<DataElement> data;
    private String error;
}
