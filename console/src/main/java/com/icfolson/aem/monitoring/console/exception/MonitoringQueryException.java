package com.icfolson.aem.monitoring.console.exception;

public class MonitoringQueryException extends Exception {
    public MonitoringQueryException() {
    }

    public MonitoringQueryException(final String message) {
        super(message);
    }

    public MonitoringQueryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MonitoringQueryException(final Throwable cause) {
        super(cause);
    }
}
