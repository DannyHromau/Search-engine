package com.springsearchengine.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "page_data")
@NoArgsConstructor
public class PageData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String path;
    private int code;
    private byte[] content;
    @ManyToMany
    @JoinTable(name = "page_index", joinColumns = {@JoinColumn(name = "page_data_id")}, inverseJoinColumns = {@JoinColumn(name = "lemma_id")})
    private List<Lemma> lemmaList;
    @Column(name = "site_id")
    private int siteId;

    public PageData(String path, int code, byte[] content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }

}