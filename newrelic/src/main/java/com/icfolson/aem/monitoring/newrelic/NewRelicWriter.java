package com.icfolson.aem.monitoring.newrelic;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import com.newrelic.api.agent.NewRelic;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
public class NewRelicWriter implements MonitoringWriter {

    private static final String CUSTOM_METRIC_PREFIX = "Custom";
    private static final char DIVIDER = '/';

    @Override
    public void writeEvent(final MonitoringEvent event) {
        final String name = getNewRelicName(event.getType(), true);
        NewRelic.getAgent().getInsights().recordCustomEvent(name, event.getProperties());
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        final String name = getNewRelicName(metric.getName(), true);
        NewRelic.recordMetric(name, metric.getValue());
    }

    @Override
    public void incrementCounter(final MonitoringCounter counter) {
        final String name = getNewRelicName(counter.getName(), true);
        NewRelic.incrementCounter(name, counter.getIncrement());
    }

    private static String getNewRelicName(final QualifiedName name, final boolean customPrefix) {
        if (!customPrefix || CUSTOM_METRIC_PREFIX.equals(name.getElement(0))) {
            return name.getJoined(DIVIDER);
        } else {
            return CUSTOM_METRIC_PREFIX + DIVIDER + name.getJoined(DIVIDER);
        }
    }

}
