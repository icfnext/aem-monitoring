package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;

import java.time.LocalDateTime;

public class DefaultMonitoringMetric implements MonitoringMetric {

    private final String[] name;
    private final LocalDateTime timestamp;
    private final float value;

    public DefaultMonitoringMetric(final String[] name, final float value) {
        this(name, LocalDateTime.now(), value);
    }

    public DefaultMonitoringMetric(final String[] name, final LocalDateTime timestamp, final float value) {
        this.name = name;
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public String[] getName() {
        return name;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public float getValue() {
        return value;
    }
}
