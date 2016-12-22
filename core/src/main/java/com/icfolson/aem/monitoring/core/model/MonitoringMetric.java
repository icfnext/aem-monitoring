package com.icfolson.aem.monitoring.core.model;

import java.time.LocalDateTime;

public interface MonitoringMetric {

    String[] getName();

    LocalDateTime getTimestamp();

    float getValue();

}
