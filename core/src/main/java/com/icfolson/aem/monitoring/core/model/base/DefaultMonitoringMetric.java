package com.icfolson.aem.monitoring.core.model.base;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

public class DefaultMonitoringMetric implements MonitoringMetric {

    private final QualifiedName name;
    private final long timestamp;
    private final float value;

    public DefaultMonitoringMetric(final QualifiedName name, final float value) {
        this(name, System.currentTimeMillis(), value);
    }

    public DefaultMonitoringMetric(final QualifiedName name, final long timestamp, final float value) {
        this.name = name;
        this.timestamp = timestamp;
        this.value = value;
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
    public float getValue() {
        return value;
    }
}
