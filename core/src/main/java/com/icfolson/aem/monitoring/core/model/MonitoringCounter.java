package com.icfolson.aem.monitoring.core.model;

import java.time.LocalDateTime;

public interface MonitoringCounter {

    QualifiedName getName();

    LocalDateTime getTimestamp();

    int getIncrement();

}
