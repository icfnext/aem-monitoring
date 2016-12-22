package com.icfolson.aem.monitoring.core.filter;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;

public interface MonitoringFilter {

    void filterEvent(final MonitoringEvent event, final MonitoringFilterChain filterChain);

    void filterMetric(final String[] name, final float value, final MonitoringFilterChain filterChain);

    void filterCounter(final String[] name, final int value, final MonitoringFilterChain filterChain);

}
