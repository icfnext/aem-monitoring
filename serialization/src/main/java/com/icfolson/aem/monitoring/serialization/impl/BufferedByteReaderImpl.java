package com.icfolson.aem.monitoring.serialization.impl;

import com.icfolson.aem.monitoring.serialization.BufferedByteReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class BufferedByteReaderImpl implements BufferedByteReader {

    private static final int BUFFER_SIZE = 1000;
    private static final String ENCODING = "utf-8";

    private final InputStream inputStream;
    private ByteBuffer buffer;

    private BufferedByteReaderImpl(InputStream inputStream) {
        this.inputStream = inputStream;
        byte [] array = new byte[1000];
        try {
            inputStream.read(array, 0, BUFFER_SIZE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public String getString(final short bytes) {
        final byte[] out = new byte[bytes];
        int offset = 0;
        while (buffer.remaining() < bytes - offset) {
            int remaining = buffer.remaining();
            buffer.get(out, offset, remaining);
            offset += remaining;
            advanceBuffer();
        }
        try {
            return new String(out, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public short getShort() {
        if (!remains(2)) {
            advanceBuffer();
        }
        return buffer.getShort();
    }

    private void advanceBuffer() {
        byte [] array = new byte[BUFFER_SIZE];
        final int position = buffer.position();
        final int remaining = BUFFER_SIZE - position;
        buffer.get(array, 0, remaining);
        try {
            inputStream.read(array, remaining, position);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        buffer = ByteBuffer.wrap(array);
    }

    private boolean remains(int length) {
        return buffer.remaining() >= length;
    }
}
