package com.icfolson.aem.monitoring.reporting.service;

import com.icfolson.aem.monitoring.reporting.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.reporting.model.MetricsQuery;
import com.icfolson.aem.monitoring.reporting.result.MetricsTimeSeries;

public interface MetricsQueryService {

    MetricsTimeSeries executeQuery(MetricsQuery query) throws MonitoringQueryException;

}
