package com.springsearchengine.lemmatizer;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class RussianLemmatizer implements Lemmatizer {
    private final LuceneMorphology luceneMorphology;

    public static RussianLemmatizer getInstance() {
        LuceneMorphology morphology = null;
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RussianLemmatizer(morphology);
    }

    @Override
    public HashMap<String, Double> getLemmaWithCount(String text) throws IOException {
        List<String> wordsList = getWordsList(text);
        HashMap<String, Double> lemmaStorage = new HashMap<>();
        double lemmaCount = 1;
        for (String word : wordsList) {
            List<String> wordBaseForms = luceneMorphology.getNormalForms(word.toLowerCase(Locale.ROOT));
            if (lemmaStorage.containsKey(wordBaseForms.get(0))) {
                lemmaStorage.put(wordBaseForms.get(0), lemmaStorage.get(wordBaseForms.get(0)) + 1);
            } else {
                lemmaStorage.put(wordBaseForms.get(0), lemmaCount);
            }

        }

        return lemmaStorage;
    }

    @Override
    public List<String> getWordsList(String text) throws IOException {
        List<String> wordsList = new ArrayList<>();
        text = text.replaceAll("[^А-я]+", " ");
        String[] wordsArray = text.split(" ");
        for (String word : wordsArray) {
            if (word.isEmpty()) {
                continue;
            }
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word.toLowerCase(Locale.ROOT));
            if (wordBaseForms.contains(word + "|n СОЮЗ") || wordBaseForms.contains(word + "|o МЕЖД") || wordBaseForms.contains(word + "|l ПРЕДЛ")) {
                continue;
            }
            wordsList.add(word);

        }
        return wordsList;
    }

    @Override
    public List<String> getWordBaseForms(String word) {
        return luceneMorphology.getNormalForms(word.toLowerCase(Locale.ROOT));
    }
}