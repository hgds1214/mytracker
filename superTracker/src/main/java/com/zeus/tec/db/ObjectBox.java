package com.zeus.tec.db;

import android.content.Context;

import com.zeus.tec.model.MyObjectBox;

//import com.zeus.tec.model.tracker.MyObjectBox;

import io.objectbox.BoxStore;

public class ObjectBox {
    private static BoxStore boxStore;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
    }

    public static BoxStore get() { return boxStore; }
}