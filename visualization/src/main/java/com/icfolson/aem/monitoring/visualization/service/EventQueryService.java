package com.icfolson.aem.monitoring.visualization.service;

import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.EventQuery;
import com.icfolson.aem.monitoring.visualization.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.visualization.result.EventListing;
import com.icfolson.aem.monitoring.visualization.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;

import java.util.List;

public interface EventQueryService {

    TimeSeries executeQuery(EventQuery query) throws MonitoringQueryException;

    FacetedTimeSeries executeFacetedQuery(EventQuery query) throws MonitoringQueryException;

    List<EventTypeDescriptor> getEventDescriptors() throws MonitoringQueryException;

    EventListing getEvents(EventQuery query) throws MonitoringQueryException;

}
