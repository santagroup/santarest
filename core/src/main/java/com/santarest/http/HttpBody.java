/*
 * Copyright (C) 2010 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.santarest.http;

import com.santarest.utils.MimeUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Byte array and its mime type.
 *
 * @author Bob Lee (bob@squareup.com)
 */
public abstract class HttpBody {
    private final String mimeType;
    private byte[] bytes;

    /**
     * Constructs a new typed byte array.  Sets mimeType to {@code application/unknown} if absent.
     */
    public HttpBody(String mimeType) {
        if (mimeType == null) {
            mimeType = "application/unknown";
        }
        this.mimeType = mimeType;
    }

    private byte[] bytes() {
        if (bytes == null) {
            try {
                bytes = getContent();
                if (bytes == null) {
                    throw new NullPointerException("bytes");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bytes == null) bytes = new byte[0];
        }
        return bytes;
    }

    public abstract byte[] getContent() throws IOException;

    public String fileName() {
        return null;
    }

    public String mimeType() {
        return mimeType;
    }

    public long length() {
        return bytes().length;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes());
    }

    public InputStream in() throws IOException {
        return new ByteArrayInputStream(bytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpBody that = (HttpBody) o;

        if (!Arrays.equals(bytes(), that.bytes())) return false;
        if (!mimeType.equals(that.mimeType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mimeType.hashCode();
        result = 31 * result + Arrays.hashCode(bytes());
        return result;
    }

    @Override
    public String toString() {
        String bodyCharset = MimeUtil.parseCharset(mimeType);
        try {
            return new String(bytes(), bodyCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "ByteBody[length=" + length() + "]";
    }
}