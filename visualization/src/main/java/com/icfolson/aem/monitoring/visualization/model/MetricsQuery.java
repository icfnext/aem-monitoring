package com.icfolson.aem.monitoring.visualization.model;

import java.util.ArrayList;
import java.util.List;

public class MetricsQuery {

    private final List<Short> types = new ArrayList<>();
    private Long startEpoch;
    private Long endEpoch;

    public List<Short> getTypes() {
        return types;
    }

    public Long getStartEpoch() {
        return startEpoch;
    }

    public void setStartEpoch(final Long startEpoch) {
        this.startEpoch = startEpoch;
    }

    public Long getEndEpoch() {
        return endEpoch;
    }

    public void setEndEpoch(final Long endEpoch) {
        this.endEpoch = endEpoch;
    }
}
