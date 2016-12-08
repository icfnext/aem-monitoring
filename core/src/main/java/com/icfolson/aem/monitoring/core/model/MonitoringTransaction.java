package com.icfolson.aem.monitoring.core.model;

import java.time.LocalDateTime;

/**
 * A monitoring transaction is a special case of a monitoring event that occurs over a period of time, rather than a
 * discrete time.
 */
public interface MonitoringTransaction extends MonitoringEvent {

    LocalDateTime getStartTime();

}
