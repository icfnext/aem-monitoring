package com.icfolson.aem.monitoring.core.filter;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

public interface MonitoringFilterChain {

    void filterEvent(final MonitoringEvent event);

    void filterMetric(final QualifiedName name, final float value);

    void filterCounter(final QualifiedName name, final int value);

}
