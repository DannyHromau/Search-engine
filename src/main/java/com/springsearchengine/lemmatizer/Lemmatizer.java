package com.springsearchengine.lemmatizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Lemmatizer {
    Map<String, Double> getLemmaWithCount(String text) throws IOException;

    List<String> getWordsList(String text) throws IOException;

    List<String> getWordBaseForms(String word);
}
