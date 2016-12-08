package com.icfolson.aem.monitoring.visualization.repository;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.visualization.model.EventQuery;
import com.icfolson.aem.monitoring.visualization.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.visualization.result.EventListing;
import com.icfolson.aem.monitoring.visualization.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;

import java.util.List;

public interface EventQueryRepository {

    TimeSeries executeQuery(EventQuery query) throws MonitoringDBException;

    List<EventTypeDescriptor> getEventDescriptors() throws MonitoringDBException;

    FacetedTimeSeries executeFacetedQuery(EventQuery query, int maxFacetCount) throws MonitoringDBException;

    EventListing getEvents(EventQuery query) throws MonitoringDBException;
}
