package com.icfolson.aem.monitoring.visualization.service.impl;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.MetricsQuery;
import com.icfolson.aem.monitoring.visualization.repository.MetricsQueryRepository;
import com.icfolson.aem.monitoring.visualization.result.MetricsTimeSeries;
import com.icfolson.aem.monitoring.visualization.service.MetricsQueryService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
public class MetricsQueryServiceImpl implements MetricsQueryService {

    private static final int DEFAULT_BIN_COUNT = 60;

    @Reference
    private MetricsQueryRepository repository;

    @Override
    public MetricsTimeSeries executeQuery(final MetricsQuery query) throws MonitoringQueryException {
        TimeGrouper grouper = new TimeGrouper(query.getStartEpoch(), query.getEndEpoch(), DEFAULT_BIN_COUNT);
        try {
            return repository.getMetricData(query.getType(), grouper);
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException(e);
        }
    }
}
