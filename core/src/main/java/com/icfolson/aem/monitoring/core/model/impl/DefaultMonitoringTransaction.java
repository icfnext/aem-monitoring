package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringTransaction;

import java.time.LocalDateTime;

public class DefaultMonitoringTransaction extends DefaultMonitoringEvent implements MonitoringTransaction {

    private LocalDateTime endTime;

    public DefaultMonitoringTransaction(final String type) {
        super(type);
    }

    @Override
    public LocalDateTime getTimestamp() {
        return endTime;
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getTimestamp();
    }

    public void complete() {
        endTime = LocalDateTime.now();
    }

}
