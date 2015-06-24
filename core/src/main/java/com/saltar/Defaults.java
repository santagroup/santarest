package com.saltar;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.google.gson.Gson;
import com.saltar.callback.ActionPoster;
import com.saltar.callback.EventBusPoster;
import com.saltar.callback.OttoPoster;
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

    private static class SynchronousExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            runnable.run();
        }
    }

    private static final class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable r) {
            handler.post(r);
        }
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

    public static Executor getDefaultCallbackExecutor() {
        if (isPlatform(Platform.ANDROID)) {
            return new MainThreadExecutor();
        }
        return new SynchronousExecutor();
    }

    public static ActionPoster getDefualtActionPoster() {
        if (isPlatform(Platform.ANDROID)) {
            if (hasClass("de.greenrobot.event.EventBus")) {
                return new EventBusPoster();
            }
            if (hasClass("com.squareup.otto.Bus")) {
                return new OttoPoster();
            }
        }
        return null;
    }

    private static boolean hasClass(String className) {
        boolean has = false;
        try {
            Class.forName(className);
            has = true;
        } catch (ClassNotFoundException e) {
        }
        return has;
    }

    private static boolean hasOkHttpOnClasspath() {
        if (hasClass("com.squareup.okhttp.OkUrlFactory")
                || hasClass("com.squareup.okhttp.OkHttpClient")) {
            return true;
        }
        return false;
    }


}
