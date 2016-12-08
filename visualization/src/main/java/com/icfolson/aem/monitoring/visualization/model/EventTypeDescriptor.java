package com.icfolson.aem.monitoring.visualization.model;

import java.util.ArrayList;
import java.util.List;

public class EventTypeDescriptor {

    private final String name;
    private final List<EventPropertyDescriptor> properties = new ArrayList<>();

    public EventTypeDescriptor(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<EventPropertyDescriptor> getProperties() {
        return properties;
    }
}
