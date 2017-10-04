package com.icfolson.aem.monitoring.database.system;

/**
 * An interface used to provide properties for the current system.  System properties can be used to query monitoring
 * data.  System properties are not tracked over time, and are intended to be largely static.
 */
public interface SystemInfoProvider {

    SystemInfo getSystemInfo();

}
