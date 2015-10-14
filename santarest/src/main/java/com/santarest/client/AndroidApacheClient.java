package com.santarest.client;

import android.net.http.AndroidHttpClient;

/**
 * Provides a {@link HttpClient} which uses the Android-specific version of
 * {@link org.apache.http.client.HttpClient}, {@link AndroidHttpClient}.
 * <p/>
 * If you need to provide a customized version of the {@link AndroidHttpClient} or a different
 * {@link org.apache.http.client.HttpClient} on Android use {@link ApacheClient} directly.
 */
public final class AndroidApacheClient extends ApacheClient {
    public AndroidApacheClient() {
        super(AndroidHttpClient.newInstance("Saltar"));
    }
}
