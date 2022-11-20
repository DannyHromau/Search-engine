package com.springsearchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class Statistics {
    private Total total;
    private List<DetailedStatistic> detailed;

    public Statistics(Total total, List<DetailedStatistic> detailed) {
        this.total = total;
        this.detailed = detailed;
    }
}
