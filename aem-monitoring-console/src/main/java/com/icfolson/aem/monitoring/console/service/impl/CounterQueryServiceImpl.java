package com.icfolson.aem.monitoring.console.service.impl;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.console.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.console.model.CounterQuery;
import com.icfolson.aem.monitoring.console.repository.CounterQueryRepository;
import com.icfolson.aem.monitoring.console.result.MetricsTimeSeries;
import com.icfolson.aem.monitoring.console.service.CounterQueryService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Service
@Component(immediate = true)
public class CounterQueryServiceImpl implements CounterQueryService {

    private static final int DEFAULT_BIN_COUNT = 60;

    @Reference
    private CounterQueryRepository repository;

    @Override
    public MetricsTimeSeries executeQuery(final CounterQuery query) throws MonitoringQueryException {
        TimeGrouper grouper = new TimeGrouper(query.getStartEpoch(), query.getEndEpoch(), DEFAULT_BIN_COUNT);
        try {
            return repository.getCounterData(query.getType(), grouper);
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException(e);
        }
    }
}
