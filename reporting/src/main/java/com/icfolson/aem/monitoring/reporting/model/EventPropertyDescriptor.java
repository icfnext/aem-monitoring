package com.icfolson.aem.monitoring.reporting.model;

import java.util.ArrayList;
import java.util.List;

public class EventPropertyDescriptor {

    private final String name;
    private final List<String> facets = new ArrayList<>();
    private final boolean string;
    private final boolean real;

    public EventPropertyDescriptor(final String name, final boolean string, final boolean real) {
        this.name = name;
        this.string = string;
        this.real = real;
    }

    public String getName() {
        return name;
    }

    public boolean isString() {
        return string;
    }

    public boolean isReal() {
        return real;
    }

    public List<String> getFacets() {
        return facets;
    }
}
