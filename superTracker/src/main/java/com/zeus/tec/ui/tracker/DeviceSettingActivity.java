package com.zeus.tec.ui.tracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.zeus.tec.BuildConfig;
import com.zeus.tec.databinding.ActivityDeviceSettingBinding;
import com.zeus.tec.device.usbserial.USBSerialManager;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.tracker.config.SystemConfig;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;

public class DeviceSettingActivity extends BaseActivity implements USBSerialManager.USBStateChangeListener, Handler.Callback {
    private ActivityDeviceSettingBinding binding;
    private SuperLogUtil superLogUtil;
    private Handler h = new Handler(Looper.getMainLooper(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        superLogUtil = new SuperLogUtil(this);
        EventBus.getDefault().register(this);

        binding.ivBack.setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        binding.tvLastTime.setText(SystemConfig.getLastAdjustTimeText());
        binding.tvLastAddress.setText(SystemConfig.getLastAdjustAddress());

        binding.tvOk.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            onClickAdjust();
        });

        USBSerialManager usbSerialManager = USBSerialManager.getInstance();
        usbSerialManager.addStateChangedListener(this);
        usbSerialManager.refresh(this);

        if (BuildConfig.DEBUG) {
            binding.tvShowDebug.setVisibility(View.VISIBLE);
            binding.tvShowDebug.setOnClickListener(v -> {
                FeedbackUtil.getInstance().doFeedback();
                superLogUtil.show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        USBSerialManager.getInstance().removeStateChangedListener(this);
        USBSerialManager.getInstance().release(this);
    }

    private long time;
    private int state = 0;
    private void onClickAdjust() {
        if ( state == 0) {
            step01();
        }
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    private void step01() {
        state = 1;
        time = System.currentTimeMillis();
        binding.tvCurrentTime.setText(sdf.format(time));
    }

    @Override
    public void onDeviceAttached() {
    }

    @Override
    public void onConnected() {
        binding.tvStatus.setText("设备已连接");
    }

    @Override
    public void onDisconnected() {
        binding.tvStatus.setText("等待设备连接");
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
        }
        return false;
    }
}