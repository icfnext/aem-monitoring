package com.icfolson.aem.monitoring.visualization.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EventQuery {

    private Long windowStart;
    private Long windowEnd;
    private String eventType;
    private String facetProperty;
    private String metricProperty;
    private Integer binCount;
    private final List<Predicate> predicates = new ArrayList<>();
    private final Set<UUID> systemFilter = new HashSet<>();

    public Integer getBinCount() {
        return binCount;
    }

    public void setBinCount(final Integer binCount) {
        this.binCount = binCount;
    }

    public Long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(final Long windowStart) {
        this.windowStart = windowStart;
    }

    public Long getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(final Long windowEnd) {
        this.windowEnd = windowEnd;
    }

    public String getFacetProperty() {
        return facetProperty;
    }

    public void setFacetProperty(final String facetProperty) {
        this.facetProperty = facetProperty;
    }

    public String getEventType() {
        return eventType;
    }

    public String getMetricProperty() {
        return metricProperty;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public void setMetricProperty(final String metricProperty) {
        this.metricProperty = metricProperty;
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public Set<UUID> getSystemFilter() {
        return systemFilter;
    }
}
