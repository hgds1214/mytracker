package com.zeus.tec;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.zeus.tec.db.ObjectBox;
//import com.zeus.tec.model.tracker.MyObjectBox;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.util.concurrent.TimeUnit;

import io.objectbox.android.Admin;
import okhttp3.OkHttpClient;

import com.zeus.tec.model.utils.OKHttpUpdateHttpService;
import com.zhy.http.okhttp.OkHttpUtils;

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
        initOKHttpUtils();
        initUpdate();
        if (BuildConfig.DEBUG) {
            boolean started = new Admin(ObjectBox.get()).start(this);
            LogUtils.i("ObjectBoxAdmin", "Started: " + started);
        }

        FeedbackUtil.getInstance().init(this);
    }

    private void initUpdate() {
        XUpdate.get()
                .debug(true)
                //默认设置只在wifi下检查版本更新
                .isWifiOnly(false)
                //默认设置使用get请求检查版本
                .isGet(true)
                //默认设置非自动模式，可根据具体使用配置
                .isAutoMode(false)
                //设置默认公共请求参数
                .param("versionCode", UpdateUtils.getVersionCode(this))
                .param("appKey", getPackageName())
                //设置版本更新出错的监听
                .setOnUpdateFailureListener(error -> {
                    error.printStackTrace();
                    //对不同错误进行处理
                    if (error.getCode() != 200) {
                        ToastUtils.showLong(error.toString());
                    }
                })
                //设置是否支持静默安装，默认是true
                .supportSilentInstall(false)
                //这个必须设置！实现网络请求功能。
                .setIUpdateHttpService(new OKHttpUpdateHttpService())
                //这个必须初始化
                .init(this);
    }

    private void initOKHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }
}
