package com.icfolson.aem.monitoring.core.model;

public interface MonitoringMetric {

    QualifiedName getName();

    long getTimestamp();

    float getValue();

}
