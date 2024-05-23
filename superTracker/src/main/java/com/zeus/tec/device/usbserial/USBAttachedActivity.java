package com.zeus.tec.device.usbserial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.zeus.tec.ui.HomeActivity;


public class USBAttachedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("zeus_collect", "USBAttachedActivity.onCreate()....");
        USBSerialManager.getInstance().refresh(this);
        if (ActivityUtils.getActivityList().size() <= 1) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtils.d("zeus_collect", "USBAttachedActivity.onNewIntent()....");
        if("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
            USBSerialManager.getInstance().refresh(this);
            if (ActivityUtils.getActivityList().size() <= 1) {
                startActivity(new Intent(this, HomeActivity.class));
            }
        }
        super.onNewIntent(intent);
        finish();
    }
}