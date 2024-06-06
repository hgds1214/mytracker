package com.zeus.tec.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.zeus.tec.R;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.ui.directionfinder.directionfinderActivity;
import com.zeus.tec.ui.test.zkds.EncoderWorkingActivity;
import com.zeus.tec.ui.tracker.TrackerMainActivity;
import com.zeus.tec.ui.leida.leidaMainActivity;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.JumpToPermissionUtil;


public class HomeActivity extends AppCompatActivity {


    //region  全局变量

    //endregion

    @SuppressLint({"ResourceType", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // BarUtils.transparentStatusBar(this);
        setContentView(R.layout.activity_home);

        //  llDataSendReceive  = findViewById(R.id.ll_device_list);
      //  lvDevices = findViewById(R.id.lv_devices);
        BarUtils.setStatusBarVisibility(this,true);

       // BarUtils.addMarginTopEqualStatusBarHeight(findViewById(R.layout.activity_home));
        findViewById(R.id.rl_tracker).setOnClickListener(view -> clickTracker());
        findViewById(R.id.rl_tracker_leida).setOnClickListener(view -> clickTrackerLeiDa());
        findViewById(R.id.rl_Directionfinder).setOnClickListener(View -> clickDirectionfinder());
        findViewById(R.id.rl_EncoderWorking).setOnClickListener(View->clickEncoderWorking());
       // findViewById(R.id.rl_EncoderWorking).setVisibility(View.GONE);
      //  findViewById(R.id.rl_Directionfinder).setVisibility(View.GONE);
      //  findViewById(R.id.rl_tracker).setVisibility(View.GONE);
    }

    // region leida
    private void clickTrackerLeiDa() {
        FeedbackUtil.getInstance().doFeedback();//设置音频资源
        if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            gotoLeida();
            return;
        }
    }

    private  void  gotoEncoderWorking(){
        startActivity(new Intent(HomeActivity.this, EncoderWorkingActivity.class));
    }

    private  void  clickEncoderWorking(){
        FeedbackUtil.getInstance().doFeedback();//设置音频资源

        if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            gotoEncoderWorking();
            return;
        }

    }

    private void gotoLeida() {
        MainCache cache = MainCache.GetInstance();
        String privatePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        cache.FileSavePath = privatePath;
        cache.RefreshInitFile();
        startActivity((new Intent(HomeActivity.this, leidaMainActivity.class)));
    }
    // endregion

    // region Tracker
    private void clickTracker() {
        FeedbackUtil.getInstance().doFeedback();//设置音频资源

        if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            gotoTracker();
            return;
        }
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        gotoTracker();
                    }

                    @Override
                    public void onDenied() {
                        final Dialog dialog = new Dialog(HomeActivity.this, R.style.ZeusDialog);
                        dialog.setContentView(R.layout.dialog_storage_permission);
                        dialog.findViewById(R.id.tv_ok).setOnClickListener(view -> {
                            FeedbackUtil.getInstance().doFeedback();
                            dialog.cancel();
                        });
                        dialog.findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FeedbackUtil.getInstance().doFeedback();
                                new JumpToPermissionUtil(getPackageName()).goPermissionSet(HomeActivity.this);
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                    }
                }).request();
    }

    private void gotoTracker() {
        startActivity(new Intent(HomeActivity.this, TrackerMainActivity.class));
    }
    // endregion

    //region Directionfinder
    private void clickDirectionfinder() {
        FeedbackUtil.getInstance().doFeedback();//设置音频资源
        if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            gotoDirectionfinder();
            return;
        }

    }

    private void gotoDirectionfinder() {

        startActivity(new Intent(HomeActivity.this, directionfinderActivity.class));

    }
    //endregion










}