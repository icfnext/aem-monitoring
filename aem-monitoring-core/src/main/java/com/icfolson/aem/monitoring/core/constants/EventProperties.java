package com.icfolson.aem.monitoring.core.constants;

public final class EventProperties {

    public static final String TRANSACTION_LENGTH_MS = "duration.ms";

    /**
     * The namespace reserved for system properties
     */
    public static final String SYSTEM_PROPERTY_NS = "system.";

    public static final String SYSTEM_ID = SYSTEM_PROPERTY_NS + "id";

    private EventProperties() { }
}
