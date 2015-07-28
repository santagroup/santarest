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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * File and its mime type.
 *
 * @author Bob Lee (bob@squareup.com)
 */
public class FileBody extends ByteBody {
    private static final int BUFFER_SIZE = 4096;

    private final String mimeType;
    private final File file;

    /**
     * Constructs a new typed file.
     *
     * @throws NullPointerException if file or mimeType is null
     */
    public FileBody(String mimeType, File file) {
        super(mimeType);
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        if (file == null) {
            throw new NullPointerException("file");
        }
        this.mimeType = mimeType;
        this.file = file;
    }

    /**
     * Returns the file.
     */
    public File file() {
        return file;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public String fileName() {
        return file.getName();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
        return out.toByteArray();
    }

    /**
     * Atomically moves the contents of this file to a new location.
     *
     * @param destination file
     * @throws IOException if the move fails
     */
    public void moveTo(FileBody destination) throws IOException {
        if (!mimeType().equals(destination.mimeType())) {
            throw new IOException("Type mismatch.");
        }
        if (!file.renameTo(destination.file())) {
            throw new IOException("Rename failed!");
        }
    }

    @Override
    public String toString() {
        return file.getAbsolutePath() + " (" + mimeType() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FileBody) {
            FileBody rhs = (FileBody) o;
            return file.equals(rhs.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
