package com.icfolson.aem.monitoring.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtil {

    public static final long toEpochMs(LocalDateTime time, ZoneId zoneId) {
        return time.atZone(zoneId).toInstant().toEpochMilli();
    }

    public static final LocalDateTime toLocalDateTime(final long epochMs, ZoneId zoneId) {
        return LocalDateTime.ofEpochSecond(epochMs / 1000, (int) epochMs % 1000,
            zoneId.getRules().getOffset(Instant.ofEpochMilli(epochMs)));
    }

    private TimeUtil() { }

}
