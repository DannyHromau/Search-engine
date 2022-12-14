package com.springsearchengine.controller;


import com.springsearchengine.dto.PageToPost;
import com.springsearchengine.dto.SearchRequest;
import com.springsearchengine.dto.SimpleResponse;
import com.springsearchengine.dto.data.SearchPageData;
import com.springsearchengine.dto.statistics.GeneralInfo;
import com.springsearchengine.service.SearchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    @Autowired
    private SearchingService searchingService;
    @Value(value = "${resultPageLimit}")
    private int pageCount;

    @GetMapping("/startIndexing")
    public SimpleResponse startIndexing() {
        return searchingService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public SimpleResponse stopIndexing() {
        SimpleResponse simpleResponse = searchingService.stopIndexing();
        return simpleResponse;
    }

    @PostMapping("/indexPage")
    public SimpleResponse postPage(PageToPost pageToPost) {
        SimpleResponse simpleResponse = searchingService.savePageByUrl(pageToPost.getUrl());
        return simpleResponse;
    }

    @GetMapping("/statistics")
    public GeneralInfo getStatistics() {
        GeneralInfo generalInfo = searchingService.getStatistics();
        return generalInfo;
    }

    @GetMapping("/search")
    public SearchPageData getSearchingResult(SearchRequest searchRequest) {

        SearchPageData searchPageData = searchingService.getSearchPage(searchRequest.getQuery(), pageCount, searchRequest.getSite());
        return searchPageData;
    }
}
