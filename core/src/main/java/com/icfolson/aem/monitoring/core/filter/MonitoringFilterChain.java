package com.icfolson.aem.monitoring.core.filter;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;

public interface MonitoringFilterChain {

    void filterEvent(final MonitoringEvent event);

    void filterMetric(final String[] name, final float value);

    void filterCounter(final String[] name, final int value);

}
