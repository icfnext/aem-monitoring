package com.icfolson.aem.monitoring.visualization.repository;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.visualization.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;

import java.util.List;

public interface MetricsQueryRepository {

    TimeSeries getMetricData(final short metricId, final TimeGrouper timeGrouper) throws MonitoringDBException;

    FacetedTimeSeries getMetricData(final List<Short> metricIds, final TimeGrouper timeGrouper)
        throws MonitoringDBException;

}
