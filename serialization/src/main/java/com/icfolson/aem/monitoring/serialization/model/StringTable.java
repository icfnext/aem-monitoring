package com.icfolson.aem.monitoring.serialization.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class for writing serialized strings.  Not thread-safe.
 */
public class StringTable {

    private static final Logger LOG = LoggerFactory.getLogger(StringTable.class);

    private final BiMap<String, Short> table = HashBiMap.create();
    private short nextIndex = 0;

    public short addString(final String value) {
        return table.computeIfAbsent(value, k -> nextIndex++);
    }

    public String getString(short index) {
        return table.inverse().get(index);
    }

    public void writeTable(final DataOutputStream output) {
        try {
            final BiMap<Short, String> inverse = table.inverse();
            output.writeShort(nextIndex);
            for (short i = 0; i < nextIndex; i++) {
                output.writeUTF(inverse.get(i));
            }
        } catch (IOException e) {
            LOG.error("Error writing table", e);
        }
    }

    public static StringTable readTable(final DataInputStream input) {
        StringTable out = new StringTable();
        short length = -2;
        try {
            length = input.readShort();
            for (short i = 0; i < length; i++) {
                short index = out.addString(input.readUTF());
                if (index != i) {
                    LOG.error("Error reading string table. i={}, index={}", i, index);
                }
            }
            return out;
        } catch (IOException e) {
            LOG.error("Error reading table. length = " + length, e);
        }
        return out;
    }

}
