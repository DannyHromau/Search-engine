package com.springsearchengine.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "page_index")
@NoArgsConstructor
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "lemma_id")
    private int lemmaId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", insertable = false, updatable = false)
    private Lemma lemma;
    @Column(name = "page_data_id")
    private int pageDataId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_data_id", insertable = false, updatable = false)
    private PageData pageData;
    private double ran;

    public Index(Lemma lemma, PageData pageData, double ran) {
        this.pageData = pageData;
        this.lemma = lemma;
        this.ran = ran;
    }
}