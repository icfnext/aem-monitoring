package com.icfolson.aem.monitoring.serialization.exception;

public class MonitoringSyncException extends Exception {
    public MonitoringSyncException(String message) {
        super(message);
    }

    public MonitoringSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitoringSyncException(Throwable cause) {
        super(cause);
    }
}
