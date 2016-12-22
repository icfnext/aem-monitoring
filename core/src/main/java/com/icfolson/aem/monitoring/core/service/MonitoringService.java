package com.icfolson.aem.monitoring.core.service;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;

import java.util.Map;

public interface MonitoringService {

    /**
     * Initialize a transaction.  The transaction and its properties are bound to the calling thread.
     */
    void initializeTransaction(final String category);

    /**
     * @return the name of the current transaction (or null, if not initialized)
     */
    String getTransactionType();

    /**
     * @return a copy of the current transaction properties
     */
    Map<String, Object> getTransactionProperties();

    void setTransactionProperty(final String name, final Object value);

    /**
     * Records the transaction and its properties
     */
    void recordTransaction();

    /**
     * @param event The event
     */
    void recordEvent(final MonitoringEvent event);

    /**
     * @param name The metric name
     * @param value The metric value
     */
    void recordMetric(final String[] name, final float value);

    /**
     * @param name The counter name
     * @param incrementValue The counter increment value
     */
    void incrementCounter(final String[] name, final int incrementValue);

}
