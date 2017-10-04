package com.icfolson.aem.monitoring.core.writer.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: Logging Monitoring Writer")
public class LoggingMonitoringWriter implements MonitoringWriter {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingMonitoringWriter.class);

    @Property(label = "Disable", boolValue = false, description = "Check to disable logging writer")
    private static final String DISABLE_PROP = MonitoringWriter.DISABLED_PROP;

    @Override
    public String getWriterName() {
        return "logging";
    }

    @Override
    public void writeEvent(final MonitoringEvent event) {
        LOG.trace("EVENT[type: {}, time: {}, properties: {}]", event.getType(), event.getTimestamp(),
            event.getProperties());
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        LOG.trace("METRIC[name: {}, value: {}]", metric.getName(), metric.getValue());
    }

    @Override
    public void incrementCounter(final MonitoringCounter counter) {
        LOG.trace("COUNTER[name: {}, value: {}]", counter.getName(), counter.getIncrement());
    }
}
