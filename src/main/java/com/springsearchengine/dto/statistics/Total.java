package com.springsearchengine.dto.statistics;

import lombok.Data;

@Data
public class Total {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean isIndexing;

    public Total(int sites, int pages, int lemmas, boolean isIndexing) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.isIndexing = isIndexing;
    }


}
