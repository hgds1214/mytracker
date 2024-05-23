package com.zeus.tec.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.zeus.tec.R;
import com.zeus.tec.ui.leida.ProjectleidainfoActivity;
import com.zeus.tec.ui.tracker.ProjectInfoEditActivity;


public class SplashActivity extends AppCompatActivity {

    private Handler h = new Handler();
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            finish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(this);
        setContentView(R.layout.activity_splash);
        h.postDelayed(run, 1*1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(run);
    }
}