package com.springsearchengine;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.model.IndexStorage;
import com.springsearchengine.model.entity.Site;
import com.springsearchengine.dao.IndexElementCreator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testing of getting an index")
class IndexElementCreatorTest {
    @Autowired
    FieldConfig fieldConfig;

    @Test
    @DisplayName("getting elements from document to index using exist url")
    void getIndexElements() throws IOException {
        String url = "https://dimonvideo.ru/";
        Site site = new Site();
        site.setId(1);
        Connection connection = Jsoup.connect(url);
        Document document = connection
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                .referrer("http://www.google.com")
                .timeout(5000)
                .get();
        IndexElementCreator indexElementCreator = new IndexElementCreator(document, fieldConfig, site);
        IndexStorage indexStorage = indexElementCreator.getIndexStorage();
        String actual = indexStorage.getPageData().getPath() + "," + indexStorage.getLemmaSet().size() + "," + indexStorage.getIndexSet().size();
        String expected = "https://dimonvideo.ru/,484,484";
        assertEquals(expected, actual);
    }

}
