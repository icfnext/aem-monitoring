package com.icfolson.aem.monitoring.reporting.service;

import com.icfolson.aem.monitoring.reporting.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.reporting.model.CounterQuery;
import com.icfolson.aem.monitoring.reporting.result.MetricsTimeSeries;

public interface CounterQueryService {

    MetricsTimeSeries executeQuery(CounterQuery query) throws MonitoringQueryException;

}
