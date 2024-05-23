package com.zeus.tec.device.tracker;

import android.os.SystemClock;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.BuildConfig;

import java.util.Arrays;

public class Frame {
    public byte[] frame;
    public long createTime;
    public int type = 0; // 0 其它数据 1 成功 2 失败
    public Frame(byte[] frame) {
        this.frame = frame;
        this.createTime = SystemClock.elapsedRealtime();
        if (Arrays.equals(frame, CmdManager.SUCCESS_FRAME)) {
            this.type = 1;
            if(BuildConfig.DEBUG){
                ToastUtils.showLong("解析到 成功操作 数据");
            }
        } else if (Arrays.equals(frame, CmdManager.FAILED_FRAME)) {
            this.type = 2;
            if(BuildConfig.DEBUG){
                ToastUtils.showLong("解析到 操作失败 数据");
            }
        }
    }


}
