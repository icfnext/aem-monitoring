package com.icfolson.aem.monitoring.console.service;

import com.icfolson.aem.monitoring.console.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.console.model.MetricsQuery;
import com.icfolson.aem.monitoring.console.result.MetricsTimeSeries;

public interface MetricsQueryService {

    MetricsTimeSeries executeQuery(MetricsQuery query) throws MonitoringQueryException;

}
