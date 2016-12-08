package com.icfolson.aem.monitoring.core.time;

import java.util.function.LongSupplier;
import java.util.stream.LongStream;

public class TimeGrouper {

    private final long startEpoch;
    private final long endEpoch;
    private final long binCount;
    private final long binLength;

    public TimeGrouper(final long startEpoch, final long endEpoch, final long binCount) {
        this.startEpoch = startEpoch;
        this.endEpoch = endEpoch;
        this.binCount = binCount;
        this.binLength = (endEpoch - startEpoch) / binCount;
    }

    public long getBin(final long epochMs) {
        return (epochMs - startEpoch) / binLength;
    }

    public long getBinStartTime(final long binNumber) {
        return binNumber * binLength + startEpoch;
    }

    public long getStartEpoch() {
        return startEpoch;
    }

    public long getEndEpoch() {
        return endEpoch;
    }

    public long getBinCount() {
        return binCount;
    }

    public long getBinLength() {
        return binLength;
    }

    public LongStream getPoints() {
        LongSupplier supplier = new LongSupplier() {
            private long current = startEpoch - binLength;
            @Override
            public long getAsLong() {
                current += binLength;
                return current;
            }
        };
        return LongStream.generate(supplier).limit(binCount);
    }
}
