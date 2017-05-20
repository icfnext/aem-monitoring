package com.icfolson.aem.monitoring.console.model;

public class Predicate {

    private final String propertyName;
    private final Operation operation;
    private final String value;

    public Predicate(final String propertyName, final Operation operation, final String value) {
        this.propertyName = propertyName;
        this.operation = operation;
        this.value = value;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getValue() {
        return value;
    }
}
