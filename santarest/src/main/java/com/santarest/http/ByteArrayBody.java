package com.santarest.http;

public class ByteArrayBody extends HttpBody {

    private final byte[] bytes;

    public ByteArrayBody(String mimeType, byte[] bytes) {
        super(mimeType);
        this.bytes = bytes;
    }

    public byte[] getContent() {
        return bytes;
    }
}