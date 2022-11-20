package com.springsearchengine.service;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.config.ListOfSitesMetaData;
import com.springsearchengine.dao.IndexElementCreator;
import com.springsearchengine.dao.ManagerThread;
import com.springsearchengine.dao.SearchEngineManager;
import com.springsearchengine.dto.SearchPage;
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
    @Autowired
    private SearchEngineManager manager;
    private static List<Thread> indexThreadsList = new ArrayList<>();
    private static List<SearchEngineManager> searchEngineManagerList = new ArrayList<>();
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SimpleResponse startIndexing() {
        SearchEngineManager.toStop = false;
        SimpleResponse simpleResponse = new SimpleResponse();
        int count = 0;
        List<Site> sitesFromDB = (List<Site>) siteRepository.findAll();
        for (Site site : sitesFromDB) {
            if (site.getStatus().equals(Status.INDEXING)) {
                count++;
            }
        }

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
            List<Site> siteList = (List<Site>) siteRepository.findAll();
            boolean siteExist = false;
            boolean pageExist;
            for (Site site : siteList) {
                if (path.contains(site.getUrl())) {
                    siteExist = true;
                    pageExist = manager.pageExist(path, site.getId());
                    if (!pageExist) {
                        pageDataRepository.deleteBySiteId(site.getId());
                        lemmaRepository.deleteLemmaBySiteId(site.getId());
                        siteRepository.deleteSiteById(site.getId());
                        SearchEngineManager searchEngineManager = new SearchEngineManager();
                        searchEngineManager.startIndexing(site, fieldConfig);
                    }
                    break;
                }
            }
            simpleResponse.setResult(siteExist);
            if (!siteExist) {
                simpleResponse.setError("This page is not existing on the indexed list of sites");
            }
        } catch (IOException e) {
            simpleResponse.setResult(false);
            simpleResponse.setError(e.getMessage());
        }
        return simpleResponse;
    }

    public SimpleResponse savePageByUrl(String url) {
        SimpleResponse simpleResponse = new SimpleResponse();
        try {

            List<Site> siteList = (List<Site>) siteRepository.findAll();
            boolean siteExist = false;
            boolean pageExist;
            for (Site site : siteList) {
                if (url.contains(site.getUrl())) {
                    siteExist = true;
                    pageExist = manager.pageExist(url, site.getId());
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
                        List<Lemma> lemmaList = (List<Lemma>) lemmaRepository.findAll();
                        for (Lemma lemma : lemmaList) {
                            for (Lemma lemma1 : indexStorage.getLemmaSet()) {
                                if (lemma1.getLemma().equals(lemma.getLemma())) {
                                    lemma1.setFrequency(lemma.getFrequency() + 1);
                                    lemma1.setSiteId(lemma.getSiteId());
                                    lemma1.setId(lemma.getId());
                                    lemma = lemma1;
                                    lemmaRepository.save(lemma);
                                }
                                lemma1.setFrequency(1);
                                lemmaRepository.save(lemma1);
                            }
                        }
                        indexStorage.getIndexSet().forEach(indexRepository::save);

                    }
                    break;
                }
            }
            simpleResponse.setResult(siteExist);
            if (!siteExist) {
                simpleResponse.setError("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
        } catch (IOException e) {
            simpleResponse.setResult(false);
            simpleResponse.setError(e.getMessage());
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

    public SearchPageData getSearchPage(String searchRequest, int limit) {
        SearchPageData searchPageData = new SearchPageData();
        List<DataElement> dataElements = new ArrayList<>();
        List<SearchPage> searchPageList = searchEngineManager.getSearchPageList(searchRequest);
        if (searchEngineManager.getSearchPageList(searchRequest).size() > 0) {
            searchPageData.setResult(true);
            for (SearchPage searchPage : searchPageList) {
                String regexUri = "https:\\/\\/[A-z]+\\.[A-z]+\\.[A-z]{2,3}";
                String regexSite = "https:\\/\\/(.+?)\\/";
                String url = searchPage.getUri();
                Matcher matcher = Pattern.compile(regexSite).matcher(url);
                String siteUrl = "";
                while (matcher.find()) {
                    siteUrl = matcher.group(1);
                }
                siteUrl = "https://" + siteUrl;
                DataElement dataElement =
                        new DataElement(siteUrl,
                                searchPage.getUri().replaceAll("https:\\/\\/[A-z]+\\.", "")
                                        .replaceAll("\\/.+", ""),
                                searchPage.getUri().replaceAll(regexUri, ""),
                                searchPage.getTitle(),
                                searchPage.getSnippet(),
                                searchPage.getRelevance());
                dataElements.add(dataElement);
            }
            dataElements = limit > 0 ? dataElements
                    .stream()
                    .limit(limit)
                    .toList() : dataElements;
            searchPageData.setData(dataElements);
            searchPageData.setCount(searchEngineManager.getSearchPageList(searchRequest).size());

        } else {
            searchPageData.setResult(false);
            searchPageData.setError("Задан пустой поисковый запрос");
        }
        return searchPageData;
    }


}
