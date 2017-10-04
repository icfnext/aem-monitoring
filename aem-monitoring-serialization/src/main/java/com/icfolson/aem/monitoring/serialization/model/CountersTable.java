package com.icfolson.aem.monitoring.serialization.model;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringCounter;
import com.icfolson.aem.monitoring.core.util.NameUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CountersTable {

    private final List<MonitoringCounter> counters;

    public CountersTable() {
        this(new ArrayList<MonitoringCounter>());
    }

    public CountersTable(final List<MonitoringCounter> counters) {
        this.counters = counters;
    }

    public List<MonitoringCounter> getCounters() {
        return counters;
    }

    public void addCounter(final MonitoringCounter counter) {
        counters.add(counter);
    }

    public void writeCounters(final DataOutputStream stream) {
        try (
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream metricsBuffer = new DataOutputStream(bytes);
        ) {
            final StringTable stringTable = new StringTable();
            for (final MonitoringCounter counter : counters) {
                final short nameIndex = stringTable.addString(NameUtil.toStorageFormat(counter.getName()));
                metricsBuffer.writeShort(nameIndex);
                metricsBuffer.writeLong(counter.getTimestamp());
                metricsBuffer.writeInt(counter.getIncrement());
            }
            metricsBuffer.flush();
            stringTable.writeTable(stream);
            stream.flush();
            stream.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    public static CountersTable readCounters(final DataInputStream stream) {
        final CountersTable out = new CountersTable();
        try {
            final StringTable stringTable = StringTable.readTable(stream);
            final short length = stream.readShort();
            for (short i = 0; i < length; i++) {
                final short nameIndex = stream.readShort();
                final String name = stringTable.getString(nameIndex);
                final QualifiedName qualifiedName = NameUtil.toName(name);
                final long timestamp = stream.readLong();
                final int value = stream.readInt();
                final DefaultMonitoringCounter counter = new DefaultMonitoringCounter(qualifiedName, timestamp, value);
                out.counters.add(counter);
            }
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
        return out;
    }

}
