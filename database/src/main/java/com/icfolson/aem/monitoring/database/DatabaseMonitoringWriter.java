package com.icfolson.aem.monitoring.database;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
public class DatabaseMonitoringWriter implements MonitoringWriter {

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private SystemInfoProvider systemInfoProvider;

    private MonitoringDatabase local;

    @Override
    public void writeEvent(final MonitoringEvent event) {
        local.writeEvent(event);
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        local.writeMetric(metric);
    }

    @Override
    public void incrementCounter(final MonitoringCounter counter) {
        local.writeCounter(counter);
    }

    @Activate
    @Modified
    protected void modified() {
        local = new MonitoringDatabase(systemInfoProvider.getSystemInfo(), connectionProvider);
    }

}
