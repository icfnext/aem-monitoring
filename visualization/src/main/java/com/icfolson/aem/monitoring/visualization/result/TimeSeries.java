package com.icfolson.aem.monitoring.visualization.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TimeSeries {

    public static class DataPoint implements Comparable<DataPoint> {
        private long epoch;
        private int count;
        private Float average;

        private DataPoint(final long epoch, final int count) {
            this.epoch = epoch;
            this.count = count;
        }

        public long getEpoch() {
            return epoch;
        }

        public int getCount() {
            return count;
        }

        public Float getAverage() {
            return average;
        }

        @Override
        public int compareTo(final DataPoint o) {
            return Long.compare(this.epoch, o.epoch);
        }
    }

    public class PointBuilder {
        private final long epoch;
        private final int count;
        private float average;

        private PointBuilder(final long epoch, final int count) {
            this.epoch = epoch;
            this.count = count;
        }

        public PointBuilder average(final float average) {
            this.average = average;
            return this;
        }

        public void build() {
            final DataPoint point = new DataPoint(epoch, count);
            point.average = average;
            points.put(epoch, point);
        }
    }

    private final Map<Long, DataPoint> points = new TreeMap<>();

    public List<DataPoint> getPoints() {
        return new ArrayList<>(points.values());
    }

    public int getLength() {
        return points.size();
    }

    public PointBuilder newPoint(long epoch, int count) {
        return new PointBuilder(epoch, count);
    }

    public float getAverage() {
        float power = 0;
        int total = 0;
        for (final DataPoint point : points.values()) {
            Float average = point.getAverage();
            int count = point.getCount();
            float p = (average == null ? 0 : average) * count;
            power += p;
            total += count;
        }
        return power / total;
    }

    public int getCount() {
        int total = 0;
        for (final DataPoint point : points.values()) {
            total += point.getCount();
        }
        return total;
    }

}
