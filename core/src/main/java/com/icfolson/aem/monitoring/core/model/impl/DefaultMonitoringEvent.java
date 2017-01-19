package com.icfolson.aem.monitoring.core.model.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultMonitoringEvent implements MonitoringEvent {

    private final QualifiedName type;
    private final LocalDateTime time;
    private final Map<String, Object> properties = new HashMap<>();

    public DefaultMonitoringEvent(final QualifiedName type) {
        this.type = type;
        this.time = LocalDateTime.now();
    }

    @Override
    public QualifiedName getType() {
        return type;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return time;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void setProperty(final String name, final Object value) {
        properties.put(name, value);
    }
}
