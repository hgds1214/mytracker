package com.zeus.tec;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;
import com.zeus.tec.db.ObjectBox;
//import com.zeus.tec.model.tracker.MyObjectBox;
import com.zeus.tec.model.utils.FeedbackUtil;

import io.objectbox.android.Admin;

/**
 * Created by AllenWang on 2022/8/4.
 */
public class ZeusApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "6e2924e0fa", BuildConfig.DEBUG);


        Utils.init(this);
        LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG);

        ObjectBox.init(this);
        if (BuildConfig.DEBUG) {
            boolean started = new Admin(ObjectBox.get()).start(this);
            LogUtils.i("ObjectBoxAdmin", "Started: " + started);
        }

        FeedbackUtil.getInstance().init(this);
    }
}
