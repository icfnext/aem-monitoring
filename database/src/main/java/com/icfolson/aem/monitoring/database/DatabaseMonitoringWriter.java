package com.icfolson.aem.monitoring.database;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
import com.icfolson.aem.monitoring.database.repository.EventRepository;
import com.icfolson.aem.monitoring.database.repository.MetricRepository;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
@Property(name = MonitoringWriter.NAME_PROP, value = "local")
public class DatabaseMonitoringWriter implements MonitoringWriter {

    @Reference
    private EventRepository eventRepository;

    @Reference
    private MetricRepository metricRepository;

    @Reference
    private CounterRepository counterRepository;

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


}
