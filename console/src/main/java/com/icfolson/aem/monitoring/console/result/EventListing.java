package com.icfolson.aem.monitoring.console.result;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class EventListing {

    private Map<Long, Map<String, Object>> events = new TreeMap<>();
    private Set<String> propertyNames = new HashSet<>();

    public void addEvent(final long eventId, final long timestamp, final UUID systemId) {
        final Map<String, Object> event = new HashMap<>();
        event.put("timestamp", timestamp);
        event.put("system.ID", systemId.toString());
        events.put(eventId, event);
    }

    public void addEventProperty(final long eventId, final String propertyName, final Object value) {
        final Map<String, Object> event = events.get(eventId);
        assert event != null;
        event.put(propertyName, value);
        propertyNames.add(propertyName);
    }

    public Collection<Map<String, Object>> getEvents() {
        return events.values();
    }

    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    public Set<Long> getEventIds() {
        return events.keySet();
    }
}
