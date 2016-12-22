package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;

import java.time.LocalDateTime;

public class DefaultMonitoringCounter implements MonitoringCounter {

    private final String[] name;
    private final LocalDateTime timestamp;
    private final int increment;

    public DefaultMonitoringCounter(final String[] name, final int increment) {
        this(name, LocalDateTime.now(), increment);
    }

    public DefaultMonitoringCounter(final String[] name, final LocalDateTime timestamp, final int increment) {
        this.name = name;
        this.timestamp = timestamp;
        this.increment = increment;
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
    public int getIncrement() {
        return increment;
    }
}
