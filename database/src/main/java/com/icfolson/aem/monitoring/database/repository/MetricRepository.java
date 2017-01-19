package com.icfolson.aem.monitoring.database.repository;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;

import java.util.List;
import java.util.Map;

public interface MetricRepository {

    Map<String, Short> getMetricTypes();

    void writeMetric(final MonitoringMetric metric);

    List<MonitoringMetric> getMetrics(Long since, Integer limit);

}
