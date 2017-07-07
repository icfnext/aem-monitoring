package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.repository.MetricRepository;
import com.icfolson.aem.monitoring.database.repository.SystemRepository;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.system.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.writer.MetricsDatabase;
import org.apache.felix.scr.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true)
public class MetricRepositoryImpl implements MetricRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MetricRepositoryImpl.class);

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private SystemRepository systemRepository;

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
        try {
            final SystemInfo systemInfo = systemInfoProvider.getSystemInfo();
            final String repositoryUuid = systemInfo.getSystemId().toString();
            final short systemId = systemRepository.getSystemId(repositoryUuid);
            writer = new MetricsDatabase(systemId, connectionProvider);
        } catch (MonitoringDBException e) {
            LOG.error("Error starting Metric Repository", e);
        }
    }

}
