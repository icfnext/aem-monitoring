package com.icfolson.aem.monitoring.serialization.model;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.impl.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.database.util.NameUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventsTable {

    private final List<MonitoringEvent> events;

    public EventsTable() {
        this(new ArrayList<MonitoringEvent>());
    }

    public EventsTable(final List<MonitoringEvent> events) {
        this.events = events;
    }

    public static EventsTable readEvents(final InputStream inputStream) {
        final EventsTable out = new EventsTable();
        try (final DataInputStream stream = new DataInputStream(inputStream)) {
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
            e.printStackTrace(); // TODO
        }
        return out;
    }

    public void writeTable(final OutputStream outputStream) {

        final StringTable stringTable = new StringTable();
        try (
            final ByteArrayOutputStream eventByteBuffer = new ByteArrayOutputStream();
            final DataOutputStream stream = new DataOutputStream(outputStream);
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
            e.printStackTrace(); // TODO
        }
    }

    public List<MonitoringEvent> getEvents() {
        return events;
    }
}
