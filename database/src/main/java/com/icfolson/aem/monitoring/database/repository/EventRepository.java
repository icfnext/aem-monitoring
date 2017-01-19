package com.icfolson.aem.monitoring.database.repository;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;

import java.util.Map;

public interface EventRepository {

    Map<String, Short> getEventTypeMap();

    void writeEvent(final MonitoringEvent event);

}
