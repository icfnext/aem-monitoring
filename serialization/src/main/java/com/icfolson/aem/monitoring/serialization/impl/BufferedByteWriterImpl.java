package com.icfolson.aem.monitoring.serialization.impl;

import com.icfolson.aem.monitoring.serialization.BufferedByteWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class BufferedByteWriterImpl implements BufferedByteWriter {

    private static final String ENCODING = "utf-8";

    private final OutputStream outputStream;

    public BufferedByteWriterImpl(final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeString(final String value) {
        try {
            byte[] bytes = value.getBytes(ENCODING);
            if (bytes.length > Short.MAX_VALUE) {
                throw new IllegalArgumentException("The max length for strings is " + Short.MAX_VALUE);
            }
            writeShort((short) bytes.length);
            outputStream.write(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void writeShort(final short value) {

    }
}
