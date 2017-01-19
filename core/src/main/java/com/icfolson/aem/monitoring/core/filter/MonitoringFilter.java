package com.icfolson.aem.monitoring.core.filter;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

public interface MonitoringFilter {

    void filterEvent(final MonitoringEvent event, final MonitoringFilterChain filterChain);

    void filterMetric(final QualifiedName name, final float value, final MonitoringFilterChain filterChain);

    void filterCounter(final QualifiedName name, final int value, final MonitoringFilterChain filterChain);

}
