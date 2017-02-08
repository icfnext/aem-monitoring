package com.icfolson.aem.monitoring.reporting.service;

import com.icfolson.aem.monitoring.reporting.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.reporting.model.EventQuery;
import com.icfolson.aem.monitoring.reporting.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.reporting.result.EventListing;
import com.icfolson.aem.monitoring.reporting.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.reporting.result.TimeSeries;

import java.util.List;

public interface EventQueryService {

    TimeSeries executeQuery(EventQuery query) throws MonitoringQueryException;

    FacetedTimeSeries executeFacetedQuery(EventQuery query) throws MonitoringQueryException;

    List<EventTypeDescriptor> getEventDescriptors() throws MonitoringQueryException;

    EventListing getEvents(EventQuery query) throws MonitoringQueryException;

}
