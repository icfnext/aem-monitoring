package com.icfolson.aem.monitoring.database.repository;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;

import java.util.List;
import java.util.Map;

public interface CounterRepository {

    Map<String, Short> getCounterNameHierarchy();

    void writeCounter(final MonitoringCounter counter);

    List<MonitoringCounter> getCounters(Long since, Integer limit);

}
