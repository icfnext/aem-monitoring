package com.icfolson.aem.monitoring.core.model;

import java.time.LocalDateTime;

public interface MonitoringCounter {

    String getName();

    LocalDateTime getTimestamp();

    int getIncrement();

}
