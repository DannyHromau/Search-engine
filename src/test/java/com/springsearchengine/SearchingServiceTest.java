package com.springsearchengine;

import com.springsearchengine.SearchEngineApp;
import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.config.ListOfSitesMetaData;
import com.springsearchengine.dao.SearchEngineManager;
import com.springsearchengine.dto.SimpleResponse;
import com.springsearchengine.model.entity.Site;
import com.springsearchengine.model.entity.Status;
import com.springsearchengine.repositories.IndexRepository;
import com.springsearchengine.repositories.LemmaRepository;
import com.springsearchengine.repositories.PageDataRepository;
import com.springsearchengine.repositories.SiteRepository;
import com.springsearchengine.service.SearchingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@DisplayName("Testing of searching service")
@SpringBootTest(classes = SearchEngineApp.class)
class SearchingServiceTest {


    @InjectMocks
    private SearchingService service;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private SearchEngineManager searchEngineManager;
    @Mock
    private PageDataRepository pageDataRepository;
    @Mock
    private IndexRepository indexRepository;
    @Mock
    private LemmaRepository lemmaRepository;
    @Mock
    private ListOfSitesMetaData listOfSitesMetaData;
    @Mock
    private FieldConfig fieldConfig;
    @Mock
    private JdbcTemplate jdbcTemplate;
    private ListOfSitesMetaData.SiteList siteList = new ListOfSitesMetaData.SiteList();
    private Site site;
    private List<Site> sitesList = new ArrayList<>();
    private static String url = "https://test.test/";
    private List<ListOfSitesMetaData.SiteList> metaList = new ArrayList<>();



    @BeforeEach
    public void setup(){
        site = new Site();
        siteList.setUrl("https://test.test");
        metaList.add(siteList);
        SearchEngineManager.toStop = true;
        site.setUrl(url);
        sitesList.add(site);
        when(listOfSitesMetaData.getSiteList()).thenReturn(metaList);
        when(siteRepository.findAll()).thenReturn(sitesList);
        when(siteRepository.findSiteByUrl(siteList.getUrl())).thenReturn(site);
        doCallRealMethod().when(searchEngineManager).checkPathRightFormat(anyString());

    }
    @Test
    @DisplayName("Stopping indexing when it is started")
    void stopIndexingWhenItNotStarted() {
        site.setStatus(Status.INDEXING);
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(true);
        SimpleResponse actual = service.stopIndexing();
        Assertions.assertEquals(expected, actual);
    }
    @Test
    @DisplayName("Stopping indexing when it is not started")
    void stopIndexingWhenItStarted(){
        site.setStatus(Status.INDEXED);
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(false);
        expected.setError("Indexing is not begin");
        SimpleResponse actual = service.stopIndexing();
        Assertions.assertEquals(expected, actual);
    }
    @Test
    @DisplayName("Starting indexing when it is started")
    void startIndexingWhenItStarted(){
        when(siteRepository.countByStatus(Status.INDEXING)).thenReturn(1);
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(false);
        expected.setError("Indexing is running");
        SimpleResponse actual = service.startIndexing();
        assertEquals(expected, actual);
    }
    @Test
    @DisplayName("Starting indexing when it is not started")
    void startIndexingWhenItNotStarted(){
        when(siteRepository.countByStatus(Status.INDEXING)).thenReturn(0);
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(true);
        SimpleResponse actual = service.startIndexing();
        assertEquals(expected, actual);
    }
    @Test
    @DisplayName("Starting indexing for single site when it is from list of sites")
    void startIndexingSingleSiteWhenItExists(){
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(true);
        SimpleResponse actual = service.startSingleIndexing(url);
        assertEquals(expected, actual);
    }
    @Test
    @DisplayName("Starting indexing for single site when it is from list of sites")
    void startIndexingSingleSiteWhenItNotExists(){
        String wrongUrl = "https://wrong.wrong";
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(false);
        expected.setError("This page is not existing on the indexed list of sites");
        SimpleResponse actual = service.startSingleIndexing(wrongUrl);
        assertEquals(expected, actual);
    }
    @Test
    @DisplayName("Starting indexing for single site with wrong path`s format")
    void startIndexingSingleSiteWithWrongFormat(){
        String wrongUrl = "wrong.wrong";
        SimpleResponse expected = new SimpleResponse();
        expected.setResult(false);
        expected.setError("Wrong path`s format!");
        SimpleResponse actual = service.startSingleIndexing(wrongUrl);
        assertEquals(expected, actual);
    }
}
