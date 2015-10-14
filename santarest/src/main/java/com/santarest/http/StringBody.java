package com.santarest.http;

import java.io.UnsupportedEncodingException;

public class StringBody extends ByteArrayBody {

    public StringBody(String string) {
        super("text/plain; charset=UTF-8", convertToBytes(string));
    }

    private static byte[] convertToBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        try {
            return "StringBody[" + new String(getContent(), "UTF-8") + "]";
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Must be able to decode UTF-8");
        }
    }
}
