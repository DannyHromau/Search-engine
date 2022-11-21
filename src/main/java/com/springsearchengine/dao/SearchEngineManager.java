package com.springsearchengine.dao;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.dto.SearchPageResponse;
import com.springsearchengine.lemmatizer.RussianLemmatizer;
import com.springsearchengine.model.IndexStorage;
import com.springsearchengine.model.entity.*;
import com.springsearchengine.repositories.IndexRepository;
import com.springsearchengine.repositories.LemmaRepository;
import com.springsearchengine.repositories.PageDataRepository;
import com.springsearchengine.repositories.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SearchEngineManager {
    public static Set<Site> siteSet = new HashSet<>();
    public volatile static boolean toStop;
    @Autowired
    private PageDataRepository pageDataRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static RussianLemmatizer russianLemmatizer = RussianLemmatizer.getInstance();


    public void startIndexing(Site site, FieldConfig fieldConfig) {
        Set<IndexStorage> indexStorages;
        if (!toStop) {
            site.setStatus(Status.INDEXING);
            site.setLocalDateTime(LocalDateTime.now());
            siteSet.add(site);
            siteRepository.save(site);
        }
        try {
            SiteTree tree = new SiteTree(site.getUrl());
            indexStorages = new ForkJoinPool().invoke(new IndexTask(tree, fieldConfig, site));
            List<PageData> pageDataList = new ArrayList<>();
            Set<Index> indexSet = new HashSet<>();
            Set<Lemma> lemmaSet = new HashSet<>();
            for (IndexStorage indexStorage : indexStorages) {
                lemmaSet.addAll(indexStorage.getLemmaSet());
                indexStorage.getPageData().setSiteId(site.getId());
                pageDataList.add(indexStorage.getPageData());
                indexSet.addAll(indexStorage.getIndexSet());
            }
            for (Lemma lemma : lemmaSet) {
                for (Index index : indexSet) {
                    if (index.getLemma().equals(lemma)) {
                        index.setLemma(lemma);
                    }
                }
            }
            for (Lemma lemma : lemmaSet) {
                int frequency = 1;
                for (IndexStorage indexStorage : indexStorages) {
                    if (indexStorage.getLemmaSet().contains(lemma)) {
                        frequency++;
                    }
                }
                lemma.setFrequency(frequency);
                lemma.setSiteId(site.getId());
            }

            pageDataList.forEach(pageDataRepository::save);
            int indexCount = 0;
            for (Lemma lemma : lemmaSet) {
                lemmaRepository.save(lemma);
                site.setStatus(Status.INDEXING);
                site.setLocalDateTime(LocalDateTime.now());
                siteRepository.save(site);
                indexCount++;
                if (indexCount == lemmaSet.size()) {
                    site.setStatus(Status.INDEXED);
                    site.setLocalDateTime(LocalDateTime.now());
                    siteRepository.save(site);
                }
            }

            for (Index index : indexSet) {
                if (toStop) {
                    site.setStatus(Status.FAILED);
                    site.setLastError("stopped by user");
                    break;
                }
                index.setLemmaId(index.getLemma().getId());
                index.setPageDataId(index.getPageData().getId());
                indexRepository.save(index);
            }
        } catch (Exception e) {
            site.setLocalDateTime(LocalDateTime.now());
            site.setStatus(Status.FAILED);
            site.setLastError(e.toString());

        } finally {
            siteRepository.save(site);
        }


    }

    public Map<PageData, Double> getRelevantPageDataMap(String text, String site) {
        Map<PageData, Double> sortedRelevantMap = new LinkedHashMap<>();
        Site searchingSite = null;
        try {
            if (site != null){
             searchingSite = siteRepository.findSiteByUrl(site);}
            int pagesCount = jdbcTemplate.queryForObject("select count(*) from `page_data`", Integer.class);
            Map<String, Double> lemmaWithCountMap = russianLemmatizer.getLemmaWithCount(text);
            List<Lemma> searchLemmaList = new ArrayList<>();
            Map<PageData, Double> relevantPageDataMap = new HashMap<>();

                for (Map.Entry<String, Double> entry : lemmaWithCountMap.entrySet()) {
                    if (searchingSite == null) {
                        searchLemmaList = lemmaRepository.findByLemma(entry.getKey());
                    } else {
                        searchLemmaList = lemmaRepository.findLemmaByLemmaAndSiteId(entry.getKey(), searchingSite.getId());
                    }
                }
            double absRelevant = 0;
            for (Lemma lemma : searchLemmaList) {
                for (PageData pageData : lemma.getPageDataList()) {
                    Index index = indexRepository.findByLemmaIdAndPageDataId(lemma.getId(), pageData.getId());
                    if (index != null) {
                        absRelevant += index.getRan();
                        if (absRelevant > 0) {
                            relevantPageDataMap.put(pageData, absRelevant);
                        }
                    }
                }

            }
            if (relevantPageDataMap.size() > 0) {
                double maxRelevant = relevantPageDataMap
                        .entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .get().getValue();

                relevantPageDataMap
                        .entrySet()
                        .forEach(pageDataDoubleEntry -> pageDataDoubleEntry
                                .setValue(pageDataDoubleEntry.getValue() / maxRelevant));

                sortedRelevantMap = relevantPageDataMap
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry
                                .comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors
                                .toMap(Map.Entry::getKey, Map.Entry::getValue,
                                        (e1, e2) -> e1, LinkedHashMap::new));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sortedRelevantMap;
    }

    public List<SearchPageResponse> getSearchPageList(String text, String site) {
        Map<PageData, Double> relevantPageDataMap = getRelevantPageDataMap(text, site);
        List<SearchPageResponse> pages = new ArrayList<>();
        for (Map.Entry<PageData, Double> entry : relevantPageDataMap.entrySet()) {
            String uri = entry.getKey().getPath();
            double relevance = entry.getValue();
            String regex = "<title>(.+?)</title>";
            byte[] contentBytes = entry.getKey().getContent();
            String content = new String(contentBytes, StandardCharsets.UTF_8);
            Matcher matcher = Pattern.compile(regex).matcher(content);
            String title = "";
            while (matcher.find()) {
                title = matcher.group(1);
            }
            String snippet = getSnippet(content, text);
            Site checkedSite = siteRepository.findSiteById(entry.getKey().getSiteId());
            SearchPageResponse searchPageResponse = new SearchPageResponse(uri,checkedSite.getName(), title, snippet, relevance);
            pages.add(searchPageResponse);
        }
        return pages
                .stream()
                .sorted(Comparator.comparing(SearchPageResponse::getRelevance).reversed())
                .toList();
    }

    public static String getSnippet(String content, String text) {
        StringBuilder snippetBuilder = new StringBuilder();
        try {
            List<String> lemmaFromText = new ArrayList<>(russianLemmatizer.getLemmaWithCount(text).keySet());
            List<String> contentList = new ArrayList<>();
            content = content.replaceAll("[^А-я]", " ");
            content = content.trim();
            String[] array = content.split(" ");
            for (String element : array) {
                if (element.isEmpty()) {
                    continue;
                }
                contentList.add(element);
            }


            for (int i = 0; i < contentList.size(); i++) {
                for (String lemma : lemmaFromText) {
                    if (russianLemmatizer.getLemmaWithCount(contentList.get(i)).containsKey(lemma) && i >= 1 && i <= (contentList.size() - 2)) {
                        snippetBuilder
                                .append(contentList.get(i - 1))
                                .append(" ")
                                .append("<b>")
                                .append(contentList.get(i))
                                .append("</b>")
                                .append(" ")
                                .append(contentList.get(i + 1))
                                .append("...");
                    } else if (russianLemmatizer.getLemmaWithCount(contentList.get(i)).containsKey(lemma) && i >= 1 && i > (contentList.size() - 2)) {
                        snippetBuilder
                                .append(contentList.get(i - 1))
                                .append(" ")
                                .append("<b>")
                                .append(contentList.get(i))
                                .append("</b>")
                                .append("...");
                    } else if (russianLemmatizer.getLemmaWithCount(contentList.get(i)).containsKey(lemma) && i < 1 && i <= (contentList.size() - 2)) {
                        snippetBuilder
                                .append("<b>")
                                .append(contentList.get(i))
                                .append("</b>")
                                .append(" ")
                                .append(contentList.get(i + 1))
                                .append("...");
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return snippetBuilder.toString();
    }

    public boolean pageExist(String path, int siteId) throws IOException {
        boolean pageExist = false;
        List<PageData> pageDataList = pageDataRepository.findBySiteId(siteId);
        for (PageData pageData : pageDataList) {
            if (pageData.getPath().equals(path)) {
                pageExist = true;
                break;
            }
        }
        return pageExist;
    }

    public Set<Site> getSiteSet() {
        List<Site> siteList = (List<Site>) siteRepository.findAll();
        Set<Site> siteSet = new HashSet<>();
        siteSet.addAll(siteList);
        return siteSet;
    }
}