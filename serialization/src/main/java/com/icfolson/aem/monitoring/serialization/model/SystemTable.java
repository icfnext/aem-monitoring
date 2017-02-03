package com.icfolson.aem.monitoring.serialization.model;

import com.icfolson.aem.monitoring.database.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SystemTable implements SystemInfo {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTable.class);

    private UUID uuid;
    private final Map<String, String> properties = new HashMap<>();

    public static SystemTable readTable(final InputStream inputStream) {
        final SystemTable out = new SystemTable();
        try (final DataInputStream stream = new DataInputStream(inputStream)) {
            final long mostSignificant = stream.readLong();
            final long leastSignificant = stream.readLong();
            out.setUuid(new UUID(mostSignificant, leastSignificant));
            final int propertyCount = stream.readInt();
            for (int i = 0; i < propertyCount; i++) {
                final String key = stream.readUTF();
                final String value = stream.readUTF();
                out.getProperties().put(key, value);
            }
        } catch (IOException e) {
            LOG.error("Error reading system data", e);
        }
        return out;
    }

    public void writeTable(final OutputStream outputStream) {
        try (final DataOutputStream stream = new DataOutputStream(outputStream)) {
            stream.writeLong(uuid.getMostSignificantBits());
            stream.writeLong(uuid.getLeastSignificantBits());
            stream.writeInt(properties.size());
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                stream.writeUTF(entry.getKey());
                stream.writeUTF(entry.getValue());
            }
            stream.flush();
        } catch (IOException e) {
            LOG.error("Error writing system data", e);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public UUID getSystemId() {
        return uuid;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getPropertyValue(final String propertyName) {
        return properties.get(propertyName);
    }
}
