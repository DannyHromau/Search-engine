package com.springsearchengine.service;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.config.ListOfSitesMetaData;
import com.springsearchengine.dao.IndexElementCreator;
import com.springsearchengine.dao.ManagerThread;
import com.springsearchengine.dao.SearchEngineManager;
import com.springsearchengine.dto.SearchPageResponse;
import com.springsearchengine.dto.SimpleResponse;
import com.springsearchengine.dto.data.DataElement;
import com.springsearchengine.dto.data.SearchPageData;
import com.springsearchengine.dto.statistics.DetailedStatistic;
import com.springsearchengine.dto.statistics.GeneralInfo;
import com.springsearchengine.dto.statistics.Statistics;
import com.springsearchengine.dto.statistics.Total;
import com.springsearchengine.model.IndexStorage;
import com.springsearchengine.model.entity.Lemma;
import com.springsearchengine.model.entity.PageData;
import com.springsearchengine.model.entity.Site;
import com.springsearchengine.model.entity.Status;
import com.springsearchengine.repositories.IndexRepository;
import com.springsearchengine.repositories.LemmaRepository;
import com.springsearchengine.repositories.PageDataRepository;
import com.springsearchengine.repositories.SiteRepository;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@EnableConfigurationProperties(value = ListOfSitesMetaData.class)
@Log4j2
public class SearchingService {
    @Autowired
    private SearchEngineManager searchEngineManager;
    @Autowired
    private ListOfSitesMetaData listOfSitesMetaData;
    @Autowired
    private FieldConfig fieldConfig;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageDataRepository pageDataRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private LemmaRepository lemmaRepository;

    private static List<Thread> indexThreadsList = new ArrayList<>();
    private static List<SearchEngineManager> searchEngineManagerList = new ArrayList<>();
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SimpleResponse startIndexing() {
        SearchEngineManager.toStop = false;
        SimpleResponse simpleResponse = new SimpleResponse();
        int count = siteRepository.countByStatus(Status.INDEXING);

        if (count == 0) {
            jdbcTemplate.update("DELETE FROM page_data");
            jdbcTemplate.update("DELETE FROM page_index");
            jdbcTemplate.update("DELETE FROM site");
            jdbcTemplate.update("DELETE FROM lemma");
            simpleResponse.setResult(true);
            for (ListOfSitesMetaData.SiteList siteList : listOfSitesMetaData.getSiteList()) {
                if (SearchEngineManager.toStop) {
                    break;
                }
                Site site = new Site();
                site.setUrl(siteList.getUrl());
                site.setName(siteList.getName());
                ManagerThread managerThread = new ManagerThread(searchEngineManager, site, fieldConfig);
                new Thread(managerThread).start();
            }

        } else {
            simpleResponse.setResult(false);
            simpleResponse.setError("Indexing is running");
        }
        return simpleResponse;
    }

    public SimpleResponse stopIndexing() {
        SimpleResponse simpleResponse = new SimpleResponse();
        int count = 0;
        simpleResponse.setResult(true);
        List<Site> siteList = (List<Site>) siteRepository.findAll();
        for (Site site : siteList) {
            if (site.getStatus().equals(Status.INDEXING)) {
                count++;
            }
        }
        if (count > 0) {
            SearchEngineManager.toStop = true;
        } else {
            simpleResponse.setResult(false);
            simpleResponse.setError("Indexing is not begin");
        }
        return simpleResponse;
    }

    public SimpleResponse startSingleIndexing(String path) {
        SimpleResponse simpleResponse = new SimpleResponse();
        try {
            searchEngineManager.checkPathRightFormat(path);
            boolean siteExist = false;
            for (ListOfSitesMetaData.SiteList siteList : listOfSitesMetaData.getSiteList()) {
                if (path.contains(siteList.getUrl()) || siteList.getUrl().contains(path)){
                    siteExist = true;
                    Site site = siteRepository.findSiteByUrl(siteList.getUrl());
                    pageDataRepository.deleteBySiteId(site.getId());
                    lemmaRepository.deleteLemmaBySiteId(site.getId());
                    siteRepository.deleteSiteById(site.getId());
                    searchEngineManager.startIndexing(site, fieldConfig);
                    break;
                }
            }
            simpleResponse.setResult(siteExist);
            if (!siteExist) {
                simpleResponse.setError("This page is not existing on the indexed list of sites");
            }
        } catch (IllegalArgumentException e) {
            simpleResponse.setResult(false);
            simpleResponse.setError("Wrong path`s format!");
            log.warn(simpleResponse.getError());
        }
        return simpleResponse;
    }

    public SimpleResponse savePageByUrl(String url) {
        SimpleResponse simpleResponse = new SimpleResponse();
        boolean siteExist = false;
        boolean pageExist = true;
        Site site = new Site();
        try {
            searchEngineManager.checkPathRightFormat(url);
            for (ListOfSitesMetaData.SiteList siteList : listOfSitesMetaData.getSiteList()) {
                if (url.contains(siteList.getUrl()) || siteList.getUrl().contains(url)){
                    site = siteRepository.findSiteByUrl(siteList.getUrl());
                    siteExist = true;
                    pageExist = searchEngineManager.checkExistingPage(url, site.getId());
                    break;
                }
            }
                    if (!pageExist) {
                        Connection connection = Jsoup.connect(url);
                        Document document = connection
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                                .referrer("http://www.google.com")
                                .timeout(5000)
                                .get();
                        IndexElementCreator indexElementCreator = new IndexElementCreator(document, fieldConfig, site);
                        IndexStorage indexStorage = indexElementCreator.getIndexStorage();
                        pageDataRepository.save(indexStorage.getPageData());
                            for (Lemma addingLemma : indexStorage.getLemmaSet()) {
                                if (lemmaRepository.existsByLemma(addingLemma.getLemma())) {
                                    Lemma existingLemma = (Lemma) lemmaRepository.findByLemma(addingLemma.getLemma());
                                    addingLemma.setFrequency(existingLemma.getFrequency() + 1);
                                    addingLemma.setSiteId(existingLemma.getSiteId());
                                    addingLemma.setId(existingLemma.getId());
                                    existingLemma = addingLemma;
                                    lemmaRepository.save(existingLemma);
                                }
                                addingLemma.setFrequency(1);
                                lemmaRepository.save(addingLemma);
                            }

                        indexStorage.getIndexSet().forEach(indexRepository::save);
                    }
            simpleResponse.setResult(siteExist);
            if (!siteExist) {
                simpleResponse.setError("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
        } catch (IOException e) {
            simpleResponse.setResult(false);
            simpleResponse.setError(e.getMessage());
            log.warn(e.getMessage());
        } catch (IllegalArgumentException e) {
            simpleResponse.setResult(false);
            simpleResponse.setError("Wrong path`s format!");
            log.warn(simpleResponse.getError());
        }
        return simpleResponse;
    }


    public GeneralInfo getStatistics() {
        int lemmasCount = jdbcTemplate.queryForObject("select count(*) from `lemma`", Integer.class);
        int pagesCount = jdbcTemplate.queryForObject("select count(*) from `page_data`", Integer.class);
        GeneralInfo generalInfo = new GeneralInfo();
        List<Site> siteList = (List<Site>) siteRepository.findAll();
        boolean indexing = false;
        for (Site site : siteList) {
            if (site.getStatus().equals(Status.INDEXING)) {
                indexing = true;
                break;
            }
        }
        Total total = new Total(siteList.size(), pagesCount, lemmasCount, indexing);
        List<DetailedStatistic> detailed = new ArrayList<>();
        for (Site site : siteList) {
            List<PageData> pageDatumFromSite = pageDataRepository.findBySiteId(site.getId());
            List<Lemma> lemmasFromSite = lemmaRepository.findLemmaBySiteId(site.getId());
            DetailedStatistic detailedStatistic = new DetailedStatistic(site.getStatus(),
                    site.getLocalDateTime(),
                    site.getLastError(),
                    site.getUrl(),
                    site.getName(),
                    pageDatumFromSite.size(),
                    lemmasFromSite.size());
            detailed.add(detailedStatistic);
        }
        boolean result = (total.getSites() != 0) || (detailed.size() != 0);
        Statistics statistic = new Statistics(total, detailed);
        generalInfo.setResult(result);
        generalInfo.setStatistics(statistic);
        return generalInfo;
    }

    public SearchPageData getSearchPage(String searchRequest, int limit, String site) {
        SearchPageData searchPageData = new SearchPageData();
        List<DataElement> dataElements = new ArrayList<>();
        List<SearchPageResponse> searchPageResponseList = searchEngineManager.getSearchPageList(searchRequest, site);
        if (searchEngineManager.getSearchPageList(searchRequest, site).size() > 0) {
            searchPageData.setResult(true);
            for (SearchPageResponse searchPageResponse : searchPageResponseList) {
                String regexSite = "https:\\/\\/(.+?)\\/";
                String url = searchPageResponse.getUri();
                Matcher matcher = Pattern.compile(regexSite).matcher(url);
                String siteUrl = "";
                while (matcher.find()) {
                    siteUrl = matcher.group(1);
                }
                siteUrl = "https://" + siteUrl;
                DataElement dataElement =
                        new DataElement(siteUrl,
                                searchPageResponse.getName(),
                                searchPageResponse.getUri().replaceAll(siteUrl, ""),
                                searchPageResponse.getTitle(),
                                searchPageResponse.getSnippet(),
                                searchPageResponse.getRelevance());
                dataElements.add(dataElement);
            }
            dataElements = limit > 0 ? dataElements
                    .stream()
                    .limit(limit)
                    .toList() : dataElements;
            searchPageData.setData(dataElements);
            searchPageData.setCount(searchEngineManager.getSearchPageList(searchRequest, site).size());

        } else {
            searchPageData.setResult(false);
            searchPageData.setError("Задан пустой поисковый запрос");
        }
        return searchPageData;
    }


}
