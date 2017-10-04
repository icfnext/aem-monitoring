package com.icfolson.aem.monitoring.core.model.base;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultMonitoringEvent implements MonitoringEvent {

    private final QualifiedName type;
    private final long time;
    private final Map<String, Object> properties = new HashMap<>();

    public DefaultMonitoringEvent(final QualifiedName type) {
        this(type, System.currentTimeMillis());
    }

    public DefaultMonitoringEvent(final QualifiedName type, final long timestamp) {
        this.type = type;
        this.time = timestamp;
    }

    @Override
    public QualifiedName getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
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
