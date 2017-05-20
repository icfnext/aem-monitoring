package com.icfolson.aem.monitoring.reporting.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacetedTimeSeries {

    public interface Facet extends Comparable<Facet> {

        String getId();

        TimeSeries getTimeSeries();

    }

    private final Map<String, TimeSeries> facetTimeSeries = new HashMap<>();

    public TimeSeries addFacet(final String facetId) {
        TimeSeries timeSeries = new TimeSeries();
        facetTimeSeries.put(facetId, timeSeries);
        return timeSeries;
    }

    public TimeSeries getTimeSeries(final String facetId) {
        return facetTimeSeries.get(facetId);
    }

    public List<Facet> getFacets() {
        List<Facet> facets = new ArrayList<>();
        for (final Map.Entry<String, TimeSeries> entry : facetTimeSeries.entrySet()) {
            facets.add(new FacetImpl(entry.getKey(), entry.getValue()));
        }
        Collections.sort(facets);
        return facets;
    }

    private class FacetImpl implements Facet {

        private final String id;
        private final TimeSeries timeSeries;

        private FacetImpl(final String id, final TimeSeries timeSeries) {
            this.id = id;
            this.timeSeries = timeSeries;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public TimeSeries getTimeSeries() {
            return timeSeries;
        }

        @Override
        public int compareTo(final Facet o) {
            return Integer.compare(o.getTimeSeries().getCount(), this.timeSeries.getCount());
        }
    }

}
