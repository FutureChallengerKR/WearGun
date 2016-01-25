package com.karview.android.app;

import java.io.File;

import android.app.Application;

public class App extends Application {
    
    private static final boolean USE_HTTP_CACHE = false;

    @Override
    public void onCreate() {
        super.onCreate();
        
        if(USE_HTTP_CACHE){
            enableHttpResponseCache();
        }
    }

    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            File httpCacheDir = new File(getCacheDir(), "http");
            Class.forName("android.net.http.HttpResponseCache")
                .getMethod("install", File.class, long.class)
                .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
        }
    }
}