/*
 * Copyright (C) 2012 Square, Inc.
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
package com.saltar;

import com.saltar.annotations.SaltarAction;
import com.saltar.converter.Converter;
import com.saltar.http.FormUrlEncodedRequestBody;
import com.saltar.http.Header;
import com.saltar.http.MultipartRequestBody;
import com.saltar.http.Request;
import com.saltar.http.RequestBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RequestBuilder {
    private final Converter converter;
    private final String apiUrl;

    private FormUrlEncodedRequestBody formBody;
    private MultipartRequestBody multipartBody;
    private RequestBody body;

    private String path = "";
    private StringBuilder queryParams;
    private List<Header> headers;
    private String contentTypeHeader;
    private SaltarAction.Method requestMethod = SaltarAction.Method.GET;

    RequestBuilder(String apiUrl, Converter converter) {
        this.apiUrl = apiUrl;
        this.converter = converter;
        this.multipartBody = null;
        this.formBody = null;
    }

    public void setRequestType(SaltarAction.Type type) {
        switch (type) {
            case FORM_URL_ENCODED:
                formBody = new FormUrlEncodedRequestBody();
                multipartBody = null;
                body = formBody;
                break;
            case MULTIPART:
                formBody = null;
                multipartBody = new MultipartRequestBody();
                body = multipartBody;
                break;
            case SIMPLE:
                formBody = null;
                multipartBody = null;
                break;
            default:
                throw new IllegalArgumentException("Unknown request type: " + type);
        }
    }

    public void addHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null.");
        }
        if ("Content-Type".equalsIgnoreCase(name)) {
            contentTypeHeader = value;
            return;
        }

        List<Header> headers = this.headers;
        if (headers == null) {
            this.headers = headers = new ArrayList<Header>(2);
        }
        headers.add(new Header(name, value));
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void addPathParam(String name, String value) {
        addPathParam(name, value, true);
    }

    public void addEncodedPathParam(String name, String value) {
        addPathParam(name, value, false);
    }

    public void addPathParam(String name, String value, boolean urlEncodeValue) {
        if (name == null) {
            throw new IllegalArgumentException("Path replacement name must not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException(
                    "Path replacement \"" + name + "\" value must not be null.");
        }
        try {
            if (urlEncodeValue) {
                String encodedValue = URLEncoder.encode(String.valueOf(value), "UTF-8");
                // URLEncoder encodes for use as a query parameter. Path encoding uses %20 to
                // encode spaces rather than +. Query encoding difference specified in HTML spec.
                // Any remaining plus signs represent spaces as already URLEncoded.
                encodedValue = encodedValue.replace("+", "%20");
                path = path.replace("{" + name + "}", encodedValue);
            } else {
                path = path.replace("{" + name + "}", String.valueOf(value));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unable to convert path parameter \"" + name + "\" value to UTF-8:" + value, e);
        }
    }

    public void addQueryParam(String name, String value) {
        addQueryParam(name, value, false, true);
    }

    public void addEncodedQueryParam(String name, String value) {
        addQueryParam(name, value, false, false);
    }

    private void addQueryParam(String name, Object value, boolean encodeName, boolean encodeValue) {
        if (value instanceof Iterable) {
            for (Object iterableValue : (Iterable<?>) value) {
                if (iterableValue != null) { // Skip null values
                    addQueryParam(name, iterableValue.toString(), encodeName, encodeValue);
                }
            }
        } else if (value.getClass().isArray()) {
            for (int x = 0, arrayLength = Array.getLength(value); x < arrayLength; x++) {
                Object arrayValue = Array.get(value, x);
                if (arrayValue != null) { // Skip null values
                    addQueryParam(name, arrayValue.toString(), encodeName, encodeValue);
                }
            }
        } else {
            addQueryParam(name, value.toString(), encodeName, encodeValue);
        }
    }

    private void addQueryParam(String name, String value, boolean encodeName, boolean encodeValue) {
        if (name == null) {
            throw new IllegalArgumentException("Query param name must not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Query param \"" + name + "\" value must not be null.");
        }
        try {
            StringBuilder queryParams = this.queryParams;
            if (queryParams == null) {
                this.queryParams = queryParams = new StringBuilder();
            }

            queryParams.append(queryParams.length() > 0 ? '&' : '?');

            if (encodeName) {
                name = URLEncoder.encode(name, "UTF-8");
            }
            if (encodeValue) {
                value = URLEncoder.encode(value, "UTF-8");
            }
            queryParams.append(name).append('=').append(value);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unable to convert query parameter \"" + name + "\" value to UTF-8: " + value, e);
        }
    }

    private void addQueryParamMap(int parameterNumber, Map<?, ?> map, boolean encodeNames,
                                  boolean encodeValues) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object entryKey = entry.getKey();
            if (entryKey == null) {
                throw new IllegalArgumentException(
                        "Parameter #" + (parameterNumber + 1) + " query map contained null key.");
            }
            Object entryValue = entry.getValue();
            if (entryValue != null) { // Skip null values.
                addQueryParam(entryKey.toString(), entryValue.toString(), encodeNames, encodeValues);
            }
        }
    }

    public void addField(String name, Object value) {
        if (value != null) { // Skip null values.
            if (value instanceof Iterable) {
                for (Object iterableValue : (Iterable<?>) value) {
                    if (iterableValue != null) { // Skip null values.
                        formBody.addField(name, iterableValue.toString());
                    }
                }
            } else if (value.getClass().isArray()) {
                for (int x = 0, arrayLength = Array.getLength(value); x < arrayLength; x++) {
                    Object arrayValue = Array.get(value, x);
                    if (arrayValue != null) { // Skip null values.
                        formBody.addField(name, arrayValue.toString());
                    }
                }
            } else {
                formBody.addField(name, value.toString());
            }
        }
    }

    public void addPart(String name, RequestBody value, String transferEncoding) {
        multipartBody.addPart(transferEncoding, value);
    }

    public void setBody(Object value) {
        if (multipartBody != null || formBody != null) {
            throw new IllegalArgumentException("Request is not simple type");
        }
        if (value == null) {
            throw new IllegalArgumentException("Body parameter value must not be null.");
        }
        if (value instanceof RequestBody) {
            body = (RequestBody) value;
        } else {
            body = converter.toBody(value);
        }
    }

    public void setMethod(SaltarAction.Method method) {
        this.requestMethod = method;
    }

    public Request build() {
        if (multipartBody != null && multipartBody.getPartCount() == 0) {
            throw new IllegalStateException("Multipart requests must contain at least one part.");
        }
        String apiUrl = this.apiUrl;
        StringBuilder url = new StringBuilder(apiUrl);
        if (apiUrl.endsWith("/")) {
            // We require relative paths to start with '/'. Prevent a double-slash.
            url.deleteCharAt(url.length() - 1);
        }
        url.append(path);
        StringBuilder queryParams = this.queryParams;
        if (queryParams != null) {
            url.append(queryParams);
        }
        RequestBody body = this.body;
        List<Header> headers = this.headers;
        if (contentTypeHeader != null) {
            if (body != null) {
                body = new MimeOverridingTypedOutput(body, contentTypeHeader);
            } else {
                Header header = new Header("Content-Type", contentTypeHeader);
                if (headers == null) {
                    headers = Collections.singletonList(header);
                } else {
                    headers.add(header);
                }
            }
        }
        return new Request(requestMethod.name(), url.toString(), headers, body);
    }

    private static class MimeOverridingTypedOutput implements RequestBody {
        private final RequestBody delegate;
        private final String mimeType;

        MimeOverridingTypedOutput(RequestBody delegate, String mimeType) {
            this.delegate = delegate;
            this.mimeType = mimeType;
        }

        @Override
        public String fileName() {
            return delegate.fileName();
        }

        @Override
        public String mimeType() {
            return mimeType;
        }

        @Override
        public long length() {
            return delegate.length();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            delegate.writeTo(out);
        }
    }
}
