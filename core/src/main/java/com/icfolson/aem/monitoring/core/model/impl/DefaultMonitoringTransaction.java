package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringTransaction;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

import java.time.LocalDateTime;

public class DefaultMonitoringTransaction extends DefaultMonitoringEvent implements MonitoringTransaction {

    private LocalDateTime endTime;

    public DefaultMonitoringTransaction(final QualifiedName type) {
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
