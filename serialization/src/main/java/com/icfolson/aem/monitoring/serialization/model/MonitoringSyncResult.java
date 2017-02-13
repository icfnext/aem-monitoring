package com.icfolson.aem.monitoring.serialization.model;

public class MonitoringSyncResult {

    private int eventCount;
    private int metricCount;
    private int counterCount;

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public int getMetricCount() {
        return metricCount;
    }

    public void setMetricCount(int metricCount) {
        this.metricCount = metricCount;
    }

    public int getCounterCount() {
        return counterCount;
    }

    public void setCounterCount(int counterCount) {
        this.counterCount = counterCount;
    }
}
