package com.icfolson.aem.monitoring.serialization.model;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringMetric;
import com.icfolson.aem.monitoring.core.util.NameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MetricsTable {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsTable.class);

    private final List<MonitoringMetric> metrics;

    public MetricsTable() {
        this(new ArrayList<>());
    }

    public MetricsTable(final List<MonitoringMetric> metrics) {
        this.metrics = metrics;
    }

    public List<MonitoringMetric> getMetrics() {
        return metrics;
    }

    public void addMetric(final MonitoringMetric monitoringMetric) {
        metrics.add(monitoringMetric);
    }

    public void writeMetrics(final DataOutputStream stream) {
        try (
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream metricsBuffer = new DataOutputStream(bytes);
        ) {
            final StringTable stringTable = new StringTable();
            for (final MonitoringMetric metric : metrics) {
                final short nameIndex = stringTable.addString(NameUtil.toStorageFormat(metric.getName()));
                metricsBuffer.writeShort(nameIndex);
                metricsBuffer.writeLong(metric.getTimestamp());
                metricsBuffer.writeFloat(metric.getValue());
            }
            metricsBuffer.flush();
            stringTable.writeTable(stream);
            stream.writeShort(metrics.size());
            stream.flush();
            stream.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    public static MetricsTable readMetrics(final DataInputStream stream) {
        final MetricsTable out = new MetricsTable();
        try {
            final StringTable stringTable = StringTable.readTable(stream);
            final short length = stream.readShort();
            for (short i = 0; i < length; i++) {
                final short nameIndex = stream.readShort();
                final String name = stringTable.getString(nameIndex);
                final QualifiedName qualifiedName = NameUtil.toName(name);
                final long timestamp = stream.readLong();
                final float value = stream.readFloat();
                final DefaultMonitoringMetric metric = new DefaultMonitoringMetric(qualifiedName, timestamp, value);
                out.metrics.add(metric);
            }
        } catch (IOException e) {
            LOG.error("Error reading metrics", e);
        }
        return out;
    }

}
