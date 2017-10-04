package com.icfolson.aem.monitoring.database.exception;

public class MonitoringDBException extends Exception {
    public MonitoringDBException() {
    }

    public MonitoringDBException(final String message) {
        super(message);
    }

    public MonitoringDBException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MonitoringDBException(final Throwable cause) {
        super(cause);
    }
}
