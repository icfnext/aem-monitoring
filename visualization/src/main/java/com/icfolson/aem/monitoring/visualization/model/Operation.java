package com.icfolson.aem.monitoring.visualization.model;

import java.util.HashMap;
import java.util.Map;

public enum Operation {

    EQUAL("="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LIKE("LIKE");

    private final String string;

    Operation(final String string) {
        this.string = string;
    }

    private static final Map<String, Operation> BY_NAME = new HashMap<>();
    static {
    	for (Operation value: values()) {
    		BY_NAME.put(value.string, value);
    	}
    }

    public static Operation fromString(final String string) {
    	return BY_NAME.get(string);
    }

    public Predicate predicate(String property, String value) {
        return new Predicate(property, this, value);
    }

}
