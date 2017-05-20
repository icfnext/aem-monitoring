package com.icfolson.aem.monitoring.reporting.model;

public class MetricsQuery {

    private short type;
    private Long startEpoch;
    private Long endEpoch;

    public short getType() {
        return type;
    }

    public void setType(final short type) {
        this.type = type;
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
