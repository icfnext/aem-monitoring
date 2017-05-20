package com.icfolson.aem.monitoring.console.repository;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.console.result.MetricsTimeSeries;

public interface CounterQueryRepository {

    MetricsTimeSeries getCounterData(final short counterId, final TimeGrouper timeGrouper)
        throws MonitoringDBException;

}
