package com.icfolson.aem.monitoring.serialization.constants;

public final class Parameters {

    private static final String SINCE = "since";
    private static final String LIMIT = "limit";

    private static final String EVENTS = "events.";
    private static final String METRICS = "metrics.";
    private static final String COUNTERS = "counters.";

    public static final String EVENTS_SINCE = EVENTS + SINCE;
    public static final String METRICS_SINCE = METRICS + SINCE;
    public static final String COUNTERS_SINCE = COUNTERS + SINCE;

    public static final String EVENTS_LIMIT = EVENTS + LIMIT;
    public static final String METRICS_LIMIT = METRICS + LIMIT;
    public static final String COUNTERS_LIMIT = COUNTERS + LIMIT;

    private Parameters() { }
}
