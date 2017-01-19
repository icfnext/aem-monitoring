package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

import java.time.LocalDateTime;

public class DefaultMonitoringCounter implements MonitoringCounter {

    private final QualifiedName name;
    private final LocalDateTime timestamp;
    private final int increment;

    public DefaultMonitoringCounter(final QualifiedName name, final int increment) {
        this(name, LocalDateTime.now(), increment);
    }

    public DefaultMonitoringCounter(final QualifiedName name, final LocalDateTime timestamp, final int increment) {
        this.name = name;
        this.timestamp = timestamp;
        this.increment = increment;
    }

    @Override
    public QualifiedName getName() {
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
