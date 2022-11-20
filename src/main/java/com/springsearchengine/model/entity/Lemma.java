package com.springsearchengine.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "lemma")
@NoArgsConstructor
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String lemma;
    private int frequency;
    @ManyToMany
    @JoinTable(name = "page_index", joinColumns = {@JoinColumn(name = "lemma_id")}, inverseJoinColumns = {@JoinColumn(name = "page_data_id")})
    private List<PageData> pageDataList;
    @Column(name = "site_id")
    private int siteId;

    public Lemma(String lemma, int siteId) {
        this.lemma = lemma;
        this.siteId = siteId;
    }

}
