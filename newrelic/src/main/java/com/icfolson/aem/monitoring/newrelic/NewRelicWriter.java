package com.icfolson.aem.monitoring.newrelic;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import com.newrelic.api.agent.NewRelic;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
public class NewRelicWriter implements MonitoringWriter {

    private static final String CUSTOM_METRIC_PREFIX = "Custom/";

    @Override
    public void writeEvent(final MonitoringEvent event) {
        // TODO check for transaction
        // TODO alter new relic specific prop names
        NewRelic.getAgent().getInsights().recordCustomEvent(event.getType(), event.getProperties());
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        final String name = metric.getName();
        final String prefixedName = name.startsWith(CUSTOM_METRIC_PREFIX) ? name : CUSTOM_METRIC_PREFIX + name;
        NewRelic.recordMetric(prefixedName, metric.getValue());
    }

    @Override
    public void incrementCounter(final MonitoringCounter counter) {
        NewRelic.incrementCounter(counter.getName(), counter.getIncrement());
    }

}
