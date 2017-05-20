package com.icfolson.aem.monitoring.console.service;

import com.icfolson.aem.monitoring.console.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.console.model.CounterQuery;
import com.icfolson.aem.monitoring.console.result.MetricsTimeSeries;

public interface CounterQueryService {

    MetricsTimeSeries executeQuery(CounterQuery query) throws MonitoringQueryException;

}
