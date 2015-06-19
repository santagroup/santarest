package com.saltar;

import android.os.Build;
import android.os.Process;

import com.google.gson.Gson;
import com.saltar.client.AndroidApacheClient;
import com.saltar.client.HttpClient;
import com.saltar.client.OkClient;
import com.saltar.client.UrlConnectionClient;
import com.saltar.converter.Converter;
import com.saltar.converter.GsonConverter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.lang.Thread.MIN_PRIORITY;

/**
 * Created by dirong on 6/18/15.
 */
class Defaults {

    static final String THREAD_PREFIX = "Saltar-";
    static final String IDLE_THREAD_NAME = THREAD_PREFIX + "Idle";

    private enum Platform {
        ANDROID, BASE
    }

    private static Platform platform;

    private static Platform getPlatform() {
        if (platform == null) {
            platform = findPlatform();
        }
        return platform;
    }

    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return Platform.ANDROID;
            }
        } catch (ClassNotFoundException ignored) {
        }

        return Platform.BASE;
    }

    private static boolean isPlatform(Platform platform) {
        return getPlatform() == platform;
    }

    static Converter getConverter() {
        return new GsonConverter(new Gson());
    }

    static HttpClient getClient() {
        final HttpClient client;
        if (hasOkHttpOnClasspath()) {
            client = new OkClient();
        } else if (isPlatform(Platform.ANDROID) && Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            client = new AndroidApacheClient();
        } else {
            client = new UrlConnectionClient();
        }
        return client;
    }

    static Executor getDefaultHttpExecutor() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isPlatform(Platform.ANDROID)) {
                            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        } else {
                            Thread.currentThread().setPriority(MIN_PRIORITY);
                        }
                        r.run();
                    }
                }, IDLE_THREAD_NAME);
            }
        });
    }

    private static boolean hasOkHttpOnClasspath() {
        boolean okUrlFactory = false;
        try {
            Class.forName("com.squareup.okhttp.OkUrlFactory");
            okUrlFactory = true;
        } catch (ClassNotFoundException e) {
        }

        boolean okHttpClient = false;
        try {
            Class.forName("com.squareup.okhttp.OkHttpClient");
            okHttpClient = true;
        } catch (ClassNotFoundException e) {
        }

        if (!okHttpClient || !okUrlFactory) {
//            throw new RuntimeException(""
//                    + "Retrofit detected an unsupported OkHttp on the classpath.\n"
//                    + "To use OkHttp with this version of Retrofit, you'll need:\n"
//                    + "1. com.squareup.okhttp:okhttp:1.6.0 (or newer)\n"
//                    + "2. com.squareup.okhttp:okhttp-urlconnection:1.6.0 (or newer)\n"
//                    + "Note that OkHttp 2.0.0+ is supported!");
            return false;
        }

        return okHttpClient;
    }


}
