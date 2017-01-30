package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

public class DefaultMonitoringCounter implements MonitoringCounter {

    private final QualifiedName name;
    private final long timestamp;
    private final int increment;

    public DefaultMonitoringCounter(final QualifiedName name, final int increment) {
        this(name, System.currentTimeMillis(), increment);
    }

    public DefaultMonitoringCounter(final QualifiedName name, final long timestamp, final int increment) {
        this.name = name;
        this.timestamp = timestamp;
        this.increment = increment;
    }

    @Override
    public QualifiedName getName() {
        return name;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getIncrement() {
        return increment;
    }
}
