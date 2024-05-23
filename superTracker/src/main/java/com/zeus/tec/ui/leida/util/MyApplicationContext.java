package com.zeus.tec.ui.leida.util;

import android.content.Context;

public class MyApplicationContext {

    private static MyApplicationContext instance;
    private Context appContext;

    private MyApplicationContext(Context context) {
        appContext = context;
    }

    public static synchronized MyApplicationContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MyApplicationContext is not initialized. Call initialize() first.");
        }
        return instance;
    }

    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new MyApplicationContext(context);
        }
    }

    public Context getAppContext() {
        return appContext;
    }
}
