package com.springsearchengine.model;

import com.springsearchengine.model.entity.Index;
import com.springsearchengine.model.entity.Lemma;
import com.springsearchengine.model.entity.PageData;
import lombok.Data;

import java.util.Set;

@Data
public class IndexStorage {
    private PageData pageData;
    private Set<Lemma> lemmaSet;
    private Set<Index> indexSet;

    public IndexStorage(PageData pageData, Set<Lemma> lemmaSet, Set<Index> indexSet) {
        this.pageData = pageData;
        this.lemmaSet = lemmaSet;
        this.indexSet = indexSet;
    }
}
