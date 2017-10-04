package com.icfolson.aem.monitoring.console.repository;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.console.model.EventQuery;
import com.icfolson.aem.monitoring.console.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.console.result.EventListing;
import com.icfolson.aem.monitoring.console.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.console.result.TimeSeries;

import java.util.List;

public interface EventQueryRepository {

    TimeSeries executeQuery(EventQuery query) throws MonitoringDBException;

    List<EventTypeDescriptor> getEventDescriptors() throws MonitoringDBException;

    FacetedTimeSeries executeFacetedQuery(EventQuery query, int maxFacetCount) throws MonitoringDBException;

    EventListing getEvents(EventQuery query) throws MonitoringDBException;

    void deleteOldData(long deleteBeforeEpoch) throws MonitoringDBException;
}
