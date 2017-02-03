package com.icfolson.aem.monitoring.visualization.service;

import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.MetricsQuery;
import com.icfolson.aem.monitoring.visualization.result.MetricsTimeSeries;

public interface MetricsQueryService {

    MetricsTimeSeries executeQuery(MetricsQuery query) throws MonitoringQueryException;

}
