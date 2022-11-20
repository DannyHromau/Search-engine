package com.springsearchengine.dto.statistics;

import com.springsearchengine.model.entity.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DetailedStatistic {
    private String url;
    private String name;
    private Status status;
    private LocalDateTime statusTime;
    private String error;
    private int pages;
    private int lemmas;

    public DetailedStatistic(Status status, LocalDateTime statusTime, String error, String url, String name, int pages, int lemmas) {
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.url = url;
        this.name = name;
        this.pages = pages;
        this.lemmas = lemmas;
    }
}
