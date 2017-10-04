package com.icfolson.aem.monitoring.database.repository;

import com.google.common.collect.BiMap;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;

import java.util.List;

public interface EventRepository {

    BiMap<String, Short> getEventTypeMap();

    void writeEvent(final MonitoringEvent event);

    List<MonitoringEvent> getEvents(final Long since, final Integer limit);

}
