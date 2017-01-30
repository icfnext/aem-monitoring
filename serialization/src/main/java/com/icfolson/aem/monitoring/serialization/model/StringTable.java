package com.icfolson.aem.monitoring.serialization.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for writing serialized strings.  Not thread-safe.
 */
public class StringTable {

    private final BiMap<String, Short> table = HashBiMap.create();
    private short nextIndex = 0;

    public short addString(final String value) {
        Short index = table.get(value);
        if (index == null) {
            index = nextIndex++;
            table.put(value, index);
        }
        return index;
    }

    public String getString(short index) {
        return table.inverse().get(index);
    }

    public void writeTable(final DataOutputStream output) {
        try {
            List<String> strings = new ArrayList<String>(table.keySet());
            output.writeShort((short) strings.size());
            for (final String string : strings) {
                output.writeUTF(string);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static StringTable readTable(final DataInputStream input) {
        try {
            StringTable out = new StringTable();
            short length = input.readShort();
            for (short i = 0; i < length; i++) {
                out.addString(input.readUTF());
            }
            return out;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
