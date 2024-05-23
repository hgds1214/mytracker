package com.zeus.tec.ui.tracker.model;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by AllenWang on 2022/8/13.
 */
public class FunctionItem {
    public String label;
    public int resIcon;
    public Class<? extends Activity> targetCls;
    public Bundle data;

    public FunctionItem(String label, int resIcon, Class<? extends Activity> targetCls) {
        this(label, resIcon, targetCls, null);
    }
    public FunctionItem(String label, int resIcon, Class<? extends Activity> targetCls, Bundle data) {
        this.label = label;
        this.resIcon = resIcon;
        this.targetCls = targetCls;
        this.data = data;
    }

}
