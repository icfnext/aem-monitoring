package com.icfolson.aem.monitoring.database.system;

import java.util.Set;
import java.util.UUID;

public interface SystemInfo {

    UUID getSystemId();

    Set<String> getPropertyNames();

    String getPropertyValue(final String propertyName);

}
