package com.icfolson.aem.monitoring.core.filter;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

/**
 * The service interface for a monitoring filters.  If no context property is set, the filter will be executed when a
 * monitoring entity is submitted (an "input" filter).  To filter after input, but before outputting the entity to a
 * specific writer implementation, set the context to the name of the desired writer.  In both cases, the
 * 'service.ranking' property is respected.
 */
public interface MonitoringFilter {

    String CONTEXT_PROP = "filter.context";

    void filterEvent(final MonitoringEvent event, final MonitoringFilterChain filterChain);

    void filterMetric(final QualifiedName name, final float value, final MonitoringFilterChain filterChain);

    void filterCounter(final QualifiedName name, final int value, final MonitoringFilterChain filterChain);

}
