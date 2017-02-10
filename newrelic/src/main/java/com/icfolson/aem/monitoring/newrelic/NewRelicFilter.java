package com.icfolson.aem.monitoring.newrelic;

import com.icfolson.aem.monitoring.core.filter.MonitoringFilter;
import com.icfolson.aem.monitoring.core.filter.MonitoringFilterChain;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
@Property(name = MonitoringFilter.CONTEXT_PROP, value = NewRelicWriter.NAME)
public class NewRelicFilter implements MonitoringFilter {

    private static final String CUSTOM_METRIC_PREFIX = "Custom";

    @Override
    public void filterEvent(MonitoringEvent event, MonitoringFilterChain filterChain) {
        filterChain.filterEvent(event);
    }

    @Override
    public void filterMetric(QualifiedName name, float value, MonitoringFilterChain filterChain) {
        filterChain.filterMetric(name.prefix(CUSTOM_METRIC_PREFIX), value);
    }

    @Override
    public void filterCounter(QualifiedName name, int value, MonitoringFilterChain filterChain) {
        filterChain.filterCounter(name.prefix(CUSTOM_METRIC_PREFIX), value);
    }

}
