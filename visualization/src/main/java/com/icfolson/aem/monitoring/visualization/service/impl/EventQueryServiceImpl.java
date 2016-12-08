package com.icfolson.aem.monitoring.visualization.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.icfolson.aem.monitoring.core.constants.EventProperties;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.EventPropertyDescriptor;
import com.icfolson.aem.monitoring.visualization.model.EventQuery;
import com.icfolson.aem.monitoring.visualization.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.visualization.model.Predicate;
import com.icfolson.aem.monitoring.visualization.repository.EventQueryRepository;
import com.icfolson.aem.monitoring.visualization.result.EventListing;
import com.icfolson.aem.monitoring.visualization.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;
import com.icfolson.aem.monitoring.visualization.service.EventQueryService;
import com.icfolson.aem.monitoring.visualization.system.SystemRepository;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Component(immediate = true)
public class EventQueryServiceImpl implements EventQueryService {

    private static final int DEFAULT_BIN_COUNT = 60;
    private static final int DEFAULT_FACET_COUNT = 20;

    @Reference
    private EventQueryRepository events;

    @Reference
    private SystemRepository systems;

    @Override
    public TimeSeries executeQuery(final EventQuery query) throws MonitoringQueryException {
        if (query.getBinCount() == null || query.getBinCount() <= 0) {
            query.setBinCount(DEFAULT_BIN_COUNT);
        }
        try {
            convertSystemPropertiesToSystemListing(query);
            return events.executeQuery(query);
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException("Error executing query", e);
        }
    }

    @Override
    public FacetedTimeSeries executeFacetedQuery(final EventQuery query) throws MonitoringQueryException {
        try {
            convertSystemPropertiesToSystemListing(query);
            final int binCount = query.getBinCount();
            final String facetProperty = query.getFacetProperty();
            FacetedTimeSeries series = events.executeFacetedQuery(query, DEFAULT_FACET_COUNT);
            if (facetProperty.startsWith(EventProperties.SYSTEM_PROPERTY_NS)) {
                series = refacetBySystemProperty(series, facetProperty, binCount);
            }
            return series;
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException("Error executing query", e);
        }
    }

    @Override
    public List<EventTypeDescriptor> getEventDescriptors() throws MonitoringQueryException {
        try {
            List<EventTypeDescriptor> descriptors = events.getEventDescriptors();
            final Map<String, Collection<String>> mappings = systems.getDefinedSystemPropertyMappings();
            List<EventPropertyDescriptor> systemProperties = new ArrayList<>();
            for (final Map.Entry<String, Collection<String>> entry : mappings.entrySet()) {
                EventPropertyDescriptor descriptor = new EventPropertyDescriptor(entry.getKey(), true, false);
                descriptor.getFacets().addAll(entry.getValue());
                systemProperties.add(descriptor);
            }
            descriptors.forEach(item -> item.getProperties().addAll(systemProperties));
            descriptors.forEach(type -> Collections.sort(type.getProperties(),
                (o1, o2) -> o1.getName().compareTo(o2.getName())));
            return descriptors;
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException("Error getting event descriptors", e);
        }
    }

    @Override
    public EventListing getEvents(final EventQuery query) throws MonitoringQueryException {
        try {
            convertSystemPropertiesToSystemListing(query);
            return events.getEvents(query);
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException("Error getting event descriptors", e);
        }
    }

    private void convertSystemPropertiesToSystemListing(final EventQuery eventQuery) throws MonitoringQueryException {
        List<Predicate> systemPredicates = new ArrayList<>();
        new ArrayList<>(eventQuery.getPredicates()).stream()
            .filter(predicate -> predicate.getPropertyName().startsWith(EventProperties.SYSTEM_PROPERTY_NS))
            .forEach(predicate -> {
                eventQuery.getPredicates().remove(predicate);
                systemPredicates.add(predicate);
            });
        try {
            if (!systemPredicates.isEmpty()) {
                final Set<UUID> systems = this.systems.getMatchingSystems(systemPredicates);
                eventQuery.getSystemFilter().addAll(systems);
            }
        } catch (MonitoringDBException e) {
            throw new MonitoringQueryException(e);
        }
    }

    private FacetedTimeSeries refacetBySystemProperty(final FacetedTimeSeries timeSeries, final String systemProperty,
        final int binCount) throws MonitoringDBException {

        FacetedTimeSeries out = new FacetedTimeSeries();
        final Map<UUID, SystemInfo> systemInfo = this.systems.getSystemInfo();
        final Map<String, String> uuidToFacet = new HashMap<>();
        for (final SystemInfo info : systemInfo.values()) {
            uuidToFacet.put(info.getSystemId().toString(), info.getPropertyValue(systemProperty));
        }
        Multimap<String, TimeSeries> byNewFacet = ArrayListMultimap.create();
        for (final FacetedTimeSeries.Facet facet : timeSeries.getFacets()) {
            final String mappedFacetId = uuidToFacet.get(facet.getId());
            byNewFacet.put(mappedFacetId, facet.getTimeSeries());
        }
        for (final Map.Entry<String, Collection<TimeSeries>> entry : byNewFacet.asMap().entrySet()) {
            final TimeSeries series = out.addFacet(entry.getKey());
            final List<List<TimeSeries.DataPoint>> collect = entry.getValue().stream().map(ts -> ts.getPoints())
                .collect(Collectors.toList());
            for (int i = 0; i < binCount; i++) {
                float power = 0f;
                int count = 0;
                long epoch = 0;
                for (final List<TimeSeries.DataPoint> dataPoints : collect) {
                    TimeSeries.DataPoint point = dataPoints.get(i);
                    epoch = point.getEpoch();
                    count += point.getCount();
                    power += point.getCount() * point.getAverage();
                }
                series.newPoint(epoch, count).average(power / count).build();
            }
        }
        return out;
    }

}
