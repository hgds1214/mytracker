package com.zeus.tec.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.zeus.tec.ui.leida.interfaceUtil.AlarmCallback;

@SuppressLint("AppCompatCustomView")
public class CountTimeTextView extends TextView {
    public CountTimeTextView(Context context) {
        super(context);
    }

    public CountTimeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CountTimeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CountTimeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        start = false;
    }

    private long startTime;
    private boolean start = false;
    public void start() {
        start = true;
        startTime = System.currentTimeMillis();
        postDelayed(this::onTick, 500);
    }

    private long countTime;

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        refreshUI();
    }

    public void setAlarmCallback(AlarmCallback alarmCallback){
        this.alarm = alarmCallback;
    }

    //leida报警回调
    private AlarmCallback alarm;
    private boolean isAlarm =false;
    private long alarmTime =5;
    private long alarmEndTime = 25;
    public void showAlarm (long countTime){
        if (countTime==alarmTime){
            if (alarm != null) {
                if (isAlarm == false) {
                    alarm.showAlarm();
                    alarmTime = alarmEndTime;
                    isAlarm= true;
                }
                else {
                    alarm.gongAlarm();
                    // isAlarm = false;
                }
            }
        }
    }





    public void onTick() {
        if (!start) return;
        refreshUI();
        postDelayed(this::onTick, 333);
    }

    private void refreshUI() {
        long now = System.currentTimeMillis();
        countTime = (now - startTime) / 1000;

        long hms = (countTime % (24*3600));
        long h = hms / 3600;
        long m = (hms - h * 3600) / 60;
        long s = (hms - h * 3600) % 60;
        //当运行时间为alarmTime时显示提示
        showAlarm(countTime);
        setText(String.format("%02d:%02d:%02d", h, m, s));
    }

    public void stop() {
        start = false;
    }

    public long getCountTime() {
        return countTime;
    }
}
