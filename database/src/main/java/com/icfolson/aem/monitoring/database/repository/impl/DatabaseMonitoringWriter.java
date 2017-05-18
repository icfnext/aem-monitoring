package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
import com.icfolson.aem.monitoring.database.repository.EventRepository;
import com.icfolson.aem.monitoring.database.repository.MetricRepository;
import org.apache.felix.scr.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
@Component(metatype = true, label = "AEM Monitoring: Database Monitoring Writer")
@Properties({
        @Property(label = "Disable", name = MonitoringWriter.DISABLED_PROP, boolValue = false)
})
public class DatabaseMonitoringWriter implements MonitoringWriter {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseMonitoringWriter.class);

    @Reference
    private EventRepository eventRepository;

    @Reference
    private MetricRepository metricRepository;

    @Reference
    private CounterRepository counterRepository;

    public DatabaseMonitoringWriter() {
        LOG.info("Instantiating DatabaseMonitoringWriter");
    }

    @Override
    public String getWriterName() {
        return "local";
    }

    @Override
    public void writeEvent(final MonitoringEvent event) {
        eventRepository.writeEvent(event);
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        metricRepository.writeMetric(metric);
    }

    @Override
    public void incrementCounter(final MonitoringCounter counter) {
        counterRepository.writeCounter(counter);
    }

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> props) {
        LOG.info("DB Monitoring Writer started with repos: {}, {}, {}", eventRepository, metricRepository,
                counterRepository);
    }

}
