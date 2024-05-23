package com.zeus.tec.ui.tracker.config;

import com.blankj.utilcode.util.SPUtils;

import java.text.SimpleDateFormat;

public class SystemConfig {
    private static final String KEY_VOL_THREASH_HOLD = "VOL_THREASH_HOLD";
    private static final String KEY_LAST_ADJUST_TIME = "KEY_LAST_ADJUST_TIME";
    private static final String KEY_LAST_ADJUST_ADDRESS = "KEY_LAST_ADJUST_ADDRESS";
    private static float sVolThreashHold = -1;
    public static float getVolThreshHold() {
        if (sVolThreashHold == -1) {
            sVolThreashHold = SPUtils.getInstance().getFloat(KEY_VOL_THREASH_HOLD, 5.0f);
        }
        return sVolThreashHold;
    }

    public static void setsVolThreashHold(float sVolThreashHold) {
        SystemConfig.sVolThreashHold = sVolThreashHold;
        SPUtils.getInstance().put(KEY_VOL_THREASH_HOLD, sVolThreashHold, true);
    }

    public static void setLastAdjustTime(long time) {
        SPUtils.getInstance().put(KEY_LAST_ADJUST_TIME, time, true);
    }

    public static String getLastAdjustTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        long time = SPUtils.getInstance().getLong(KEY_LAST_ADJUST_TIME, 0);
        if (time == 0) {
            return "--";
        }
        return sdf.format(time);
    }


    public static String getLastAdjustAddress() {
        return SPUtils.getInstance().getString(KEY_LAST_ADJUST_ADDRESS, "--");
    }

    public static void setLastAdjustAddress(String address) {
        SPUtils.getInstance().put(KEY_LAST_ADJUST_ADDRESS, address);
    }
}
