package com.icfolson.aem.monitoring.core.model.base;

import com.icfolson.aem.monitoring.core.model.MonitoringTransaction;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

public class DefaultMonitoringTransaction extends DefaultMonitoringEvent implements MonitoringTransaction {

    private long endTime;

    public DefaultMonitoringTransaction(final QualifiedName type) {
        super(type);
    }

    @Override
    public long getTimestamp() {
        return endTime;
    }

    @Override
    public long getStartTime() {
        return super.getTimestamp();
    }

    public void complete() {
        endTime = System.currentTimeMillis();
    }

}
