package com.springsearchengine.dao;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.lemmatizer.RussianLemmatizer;
import com.springsearchengine.model.IndexStorage;
import com.springsearchengine.model.entity.Index;
import com.springsearchengine.model.entity.Lemma;
import com.springsearchengine.model.entity.PageData;
import com.springsearchengine.model.entity.Site;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexElementCreator {
    private RussianLemmatizer lemmatizer = RussianLemmatizer.getInstance();
    private FieldConfig fieldConfig;
    private Document document;
    private Site site;

    public IndexElementCreator(Document document, FieldConfig fieldConfig, Site site) throws IOException {
        this.document = document;
        this.fieldConfig = fieldConfig;
        this.site = site;
    }

    public IndexStorage getIndexStorage() throws IOException {
        Map<String, Double> lemmaMap = new HashMap<>();
        Set<Lemma> lemmaSet = new HashSet<>();
        Set<Index> indexSet = new HashSet<>();
        String pathFromDoc = document.baseUri();
        int statusCode = document.connection().execute().statusCode();
        byte[] content = document.html().getBytes(StandardCharsets.UTF_8);
        PageData pageData = new PageData(pathFromDoc, statusCode, content);
        pageData.setSiteId(site.getId());
        HashMap<String, Double> titleLemmas = new HashMap<>(lemmatizer.getLemmaWithCount(document.title()));
        HashMap<String, Double> bodyLemmas = new HashMap<>(lemmatizer.getLemmaWithCount(document.body().text()));
        titleLemmas.values().stream().map(aDouble -> aDouble * fieldConfig.getFieldList().get(0).getWeight());
        bodyLemmas.values().stream().map(aDouble -> aDouble * fieldConfig.getFieldList().get(1).getWeight());
        lemmaMap.putAll(bodyLemmas);
        for (Map.Entry<String, Double> entry : bodyLemmas.entrySet()) {
            if (lemmaMap.containsKey(entry.getKey())) {
                lemmaMap.put(entry.getKey(), entry.getValue() + lemmaMap.get(entry.getKey()));
            }
            lemmaMap.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Double> entry : lemmaMap.entrySet()) {
            Lemma lemma = new Lemma(entry.getKey(), site.getId());
            lemmaSet.add(lemma);
            Index index = new Index(lemma, pageData, entry.getValue());
            indexSet.add(index);
        }
        return new IndexStorage(pageData, lemmaSet, indexSet);
    }

}
