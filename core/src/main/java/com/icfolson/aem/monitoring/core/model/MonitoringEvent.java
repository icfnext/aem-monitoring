package com.icfolson.aem.monitoring.core.model;

import java.util.Map;

public interface MonitoringEvent {

    /**
     * @return The event type name
     */
    QualifiedName getType();

    /**
     * @return The event time
     */
    long getTimestamp();

    void setProperty(final String name, final Object value);

    /**
     * @return The event properties
     */
    Map<String, Object> getProperties();

}
