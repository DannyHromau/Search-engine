package com.searchenginetests;

import com.springsearchengine.lemmatizer.RussianLemmatizer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testing of RussianLemmatizer")
class RussianLemmatizerTest {
    RussianLemmatizer lemmatizer = RussianLemmatizer.getInstance();

    @Test
    @DisplayName("getting Lemmas with count from text")
    void getLemmasWithCount() {
        try {
            String text = "Брат брата не выдаст";
            Map<String, Double> actualMap = lemmatizer.getLemmaWithCount(text);
            Map<String, Double> expectedMap = new HashMap<>();
            expectedMap.put("выдать", 1.0);
            expectedMap.put("не", 1.0);
            expectedMap.put("брат", 2.0);
            assertEquals(expectedMap, actualMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("checking size of word's list without service words from text")
    void checkWordsListSize() {
        try {
            String text = "Повторное появление леопарда в Осетии позволяет предположить, " +
                    "что леопард постоянно обитает в некоторых районах Северного Кавказа";
            int actualSize = lemmatizer.getWordsList(text).size();
            int expectedSize = 13;
            assertEquals(expectedSize, actualSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    @DisplayName("getting word base form from word")
    void getWordBaseForms() {
        String word = "леса";
        List<String> actualList = lemmatizer.getWordBaseForms(word);
        int actualSize = actualList.size();
        int expectedSize = 2;
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("getting Lemmas with count from english text and non-letter characters")
    void getLemmasWithCountFromWrongFormatText() {
        try {
            String text = "English words >/,{";
            Map<String, Double> actualMap = lemmatizer.getLemmaWithCount(text);
            Map<String, Integer> expectedMap = new HashMap<>();
            assertEquals(expectedMap, actualMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
