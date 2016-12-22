package com.icfolson.aem.monitoring.visualization.repository;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;

public interface MetricQueryRepository {

    TimeSeries getMetricData(final long metricId, final TimeGrouper timeGrouper) throws MonitoringDBException;

}
