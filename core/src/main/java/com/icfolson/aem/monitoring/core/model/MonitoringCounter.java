package com.icfolson.aem.monitoring.core.model;

public interface MonitoringCounter {

    QualifiedName getName();

    long getTimestamp();

    int getIncrement();

}
