package com.icfolson.aem.monitoring.serialization.model;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.core.util.NameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventsTable {

    private static final Logger LOG = LoggerFactory.getLogger(EventsTable.class);

    private final List<MonitoringEvent> events;

    public EventsTable() {
        this(new ArrayList<MonitoringEvent>());
    }

    public EventsTable(final List<MonitoringEvent> events) {
        this.events = events;
    }

    public static EventsTable readEvents(final DataInputStream stream) {
        final EventsTable out = new EventsTable();
        try {
            final StringTable stringTable = StringTable.readTable(stream);
            final short eventCount = stream.readShort();
            for (short i = 0; i < eventCount; i++) {
                final short nameIndex = stream.readShort();
                final String name = stringTable.getString(nameIndex);
                final QualifiedName qualifiedName = NameUtil.toName(name);
                final long timestamp = stream.readLong();
                final DefaultMonitoringEvent event = new DefaultMonitoringEvent(qualifiedName, timestamp);
                out.events.add(event);
            }
            final int propertyCount = stream.readInt();
            for (int i = 0; i < propertyCount; i++) {
                final short eventIndex = stream.readShort();
                final short nameIndex = stream.readShort();
                final String name = stringTable.getString(nameIndex);
                final MonitoringEvent event = out.events.get(eventIndex);
                float floatVal = stream.readFloat();
                final Object value;
                if (Float.isNaN(floatVal)) {
                    final short stringIndex = stream.readShort();
                    value = stringTable.getString(stringIndex);
                } else {
                    value = floatVal;
                }
                event.setProperty(name, value);
            }
        } catch (IOException e) {
            LOG.error("Error reading table", e);
        }
        return out;
    }

    public void writeTable(final DataOutputStream stream) {

        final StringTable stringTable = new StringTable();
        try (
            final ByteArrayOutputStream eventByteBuffer = new ByteArrayOutputStream();
            final ByteArrayOutputStream propertyByteBuffer = new ByteArrayOutputStream()) {
            final DataOutputStream eventBuffer = new DataOutputStream(eventByteBuffer);
            final DataOutputStream propertyBuffer = new DataOutputStream(propertyByteBuffer);
            eventBuffer.writeShort(events.size());
            int propertyCount = 0;
            for (short i = 0; i < events.size(); i++) {
                MonitoringEvent event = events.get(i);
                final short eventNameIndex = stringTable.addString(NameUtil.toStorageFormat(event.getType()));
                eventBuffer.writeShort(eventNameIndex);
                eventBuffer.writeLong(event.getTimestamp());
                for (final Map.Entry<String, Object> entry : event.getProperties().entrySet()) {
                    final short propertyNameIndex = stringTable.addString(entry.getKey());
                    final Object value = entry.getValue();
                    if (value == null) {
                        break;
                    } else if (value instanceof String) {
                        propertyBuffer.writeShort(i);
                        propertyBuffer.writeShort(propertyNameIndex);
                        propertyBuffer.writeFloat(Float.NaN);
                        propertyBuffer.writeShort(stringTable.addString((String) value));
                        propertyCount++;
                    } else if (value instanceof Float) {
                        propertyBuffer.writeShort(i);
                        propertyBuffer.writeShort(propertyNameIndex);
                        propertyBuffer.writeFloat((Float) value);
                        propertyCount++;
                    }
                }
            }
            stringTable.writeTable(stream);
            eventBuffer.flush();
            stream.write(eventByteBuffer.toByteArray());
            stream.writeInt(propertyCount);
            propertyBuffer.flush();
            stream.write(propertyByteBuffer.toByteArray());
            stream.flush();
        } catch (IOException e) {
            LOG.error("Error writing table", e);
        }
    }

    public List<MonitoringEvent> getEvents() {
        return events;
    }
}
