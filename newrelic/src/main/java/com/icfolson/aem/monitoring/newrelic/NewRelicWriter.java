package com.icfolson.aem.monitoring.newrelic;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import com.newrelic.api.agent.NewRelic;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: New Relic Monitoring Writer")
@Properties({
        @Property(name = MonitoringWriter.NAME_PROP, value = NewRelicWriter.NAME, propertyPrivate = true),
        @Property(name = MonitoringWriter.DISABLED_PROP, boolValue = false)
})

public class NewRelicWriter implements MonitoringWriter {

    public static final String NAME = "new-relic";

    private static final char DIVIDER = '/';

    @Override
    public void writeEvent(final MonitoringEvent event) {
        final String name = event.getType().getJoined(DIVIDER);
        NewRelic.getAgent().getInsights().recordCustomEvent(name, event.getProperties());
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        final String name = metric.getName().getJoined(DIVIDER);
        NewRelic.recordMetric(name, metric.getValue());
    }

    @Override
    public void incrementCounter(final MonitoringCounter counter) {
        final String name = counter.getName().getJoined(DIVIDER);
        NewRelic.incrementCounter(name, counter.getIncrement());
    }

}
