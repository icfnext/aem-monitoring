package com.icfolson.aem.monitoring.visualization.service;

import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.MetricsQuery;
import com.icfolson.aem.monitoring.visualization.result.FacetedTimeSeries;

public interface MetricsQueryService {

    FacetedTimeSeries executeQuery(MetricsQuery query) throws MonitoringQueryException;

}
