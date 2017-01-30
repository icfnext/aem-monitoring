package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.repository.MetricRepository;
import com.icfolson.aem.monitoring.database.writer.MetricsDatabase;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true)
public class MetricRepositoryImpl implements MetricRepository {

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Reference
    private ConnectionProvider connectionProvider;

    private MetricsDatabase writer;

    @Override
    public Map<String, Short> getMetricTypes() {
        return writer.getMetricTypes();
    }

    @Override
    public void writeMetric(final MonitoringMetric metric) {
        writer.writeMetric(metric);
    }

    @Override
    public List<MonitoringMetric> getMetrics(final Long since, final Integer limit) {
        return writer.getMetrics(since, limit);
    }

    @Activate
    @Modified
    protected final void modified() {
        final SystemInfo systemInfo = systemInfoProvider.getSystemInfo();
        final ZoneId systemZone = ZoneId.systemDefault();
        writer = new MetricsDatabase(systemInfo.getSystemId(), connectionProvider);
    }

}
