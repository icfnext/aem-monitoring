package com.icfolson.aem.monitoring.console.service;

import com.icfolson.aem.monitoring.console.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.console.model.EventQuery;
import com.icfolson.aem.monitoring.console.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.console.result.EventListing;
import com.icfolson.aem.monitoring.console.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.console.result.TimeSeries;

import java.util.List;

public interface EventQueryService {

    TimeSeries executeQuery(EventQuery query) throws MonitoringQueryException;

    FacetedTimeSeries executeFacetedQuery(EventQuery query) throws MonitoringQueryException;

    List<EventTypeDescriptor> getEventDescriptors() throws MonitoringQueryException;

    EventListing getEvents(EventQuery query) throws MonitoringQueryException;

}
