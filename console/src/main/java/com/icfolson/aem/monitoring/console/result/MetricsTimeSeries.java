package com.icfolson.aem.monitoring.console.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MetricsTimeSeries {

    public interface Facet extends Comparable<Facet> {

        UUID getSystem();

        TimeSeries getTimeSeries();

    }

    private final Map<UUID, TimeSeries> facetTimeSeries = new HashMap<>();

    public TimeSeries addFacet(final UUID system) {
        TimeSeries timeSeries = new TimeSeries();
        facetTimeSeries.put(system, timeSeries);
        return timeSeries;
    }

    public TimeSeries getTimeSeries(final UUID system) {
        return facetTimeSeries.get(system);
    }

    public List<Facet> getFacets() {
        List<Facet> facets = new ArrayList<>();
        for (final Map.Entry<UUID, TimeSeries> entry : facetTimeSeries.entrySet()) {
            facets.add(new FacetImpl(entry.getKey(), entry.getValue()));
        }
        Collections.sort(facets);
        return facets;
    }

    private class FacetImpl implements Facet {

        private final UUID id;
        private final TimeSeries timeSeries;

        private FacetImpl(final UUID id, final TimeSeries timeSeries) {
            this.id = id;
            this.timeSeries = timeSeries;
        }

        @Override
        public UUID getSystem() {
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
