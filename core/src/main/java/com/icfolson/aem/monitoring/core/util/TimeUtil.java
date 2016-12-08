package com.icfolson.aem.monitoring.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtil {

    public static final long toEpochMs(LocalDateTime time, ZoneId zoneId) {
        return time.atZone(zoneId).toInstant().toEpochMilli();
    }

    private TimeUtil() { }

}
