package com.zeus.tec.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.zeus.tec.R;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.ui.directionfinder.directionfinderActivity;
import com.zeus.tec.ui.maoganDataUpload.MaoganMainActivity;
import com.zeus.tec.ui.test.zkds.EncoderWorkingActivity;
import com.zeus.tec.ui.tracker.TrackerMainActivity;
import com.zeus.tec.ui.leida.leidaMainActivity;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.JumpToPermissionUtil;
import com.zeus.tec.ui.ycs.YcsDataCollectActivity;
import com.zeus.tec.ui.ycs.YcsMainActivity;

import java.io.File;
import java.util.List;


public class HomeActivity extends AppCompatActivity {


    //region  全局变量

    //endregion

    @SuppressLint({"ResourceType", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // BarUtils.transparentStatusBar(this);
        setContentView(R.layout.activity_home);

        BarUtils.setStatusBarVisibility(this,true);
       // BarUtils.addMarginTopEqualStatusBarHeight(findViewById(R.layout.activity_home));
        findViewById(R.id.rl_tracker).setOnClickListener(view -> clickTracker());
        findViewById(R.id.rl_tracker_leida).setOnClickListener(view -> clickTrackerLeiDa());
        findViewById(R.id.rl_Directionfinder).setOnClickListener(View -> clickDirectionfinder());
        findViewById(R.id.rl_EncoderWorking).setOnClickListener(View->clickEncoderWorking());
        findViewById(R.id.rl_Ycs).setOnClickListener(v->ycsClick());
        initView();
        findViewById(R.id.device_list_txb).setOnClickListener(v -> {
            clickNumb++;
            if (clickNumb>4){
                FeedbackUtil.getInstance().doFeedback();
                showCustomDialog();
                clickNumb=0;
            }
        });
        findViewById(R.id.rl_Maogan).setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();//设置音频资源
            if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //gotoDirectionfinder();
                startActivity(new Intent(HomeActivity.this, MaoganMainActivity.class));
                return;
            }
        });
    }

    boolean wirelessIsShow;
    boolean dirctionfinderIsShow;
    boolean leidaIsShow;
    boolean trackerIsShow;
    boolean MaoganIsShow;
    boolean YcsIsShow;

    private void  initView (){
        //获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        wirelessIsShow       = sharedPreferences.getBoolean("wireless_tog",false);
        dirctionfinderIsShow = sharedPreferences.getBoolean("dirctionfinder_tog",false);
        leidaIsShow          = sharedPreferences.getBoolean("leida_tog",false);
        trackerIsShow        = sharedPreferences.getBoolean("tracker_tog",false);
        MaoganIsShow         = sharedPreferences.getBoolean("Maogan_tog",false);
        YcsIsShow            = sharedPreferences.getBoolean("Ycs_tog",false);
        if (!wirelessIsShow){
            findViewById(R.id.rl_EncoderWorking).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.rl_EncoderWorking).setVisibility(View.VISIBLE);
        }
        if (!dirctionfinderIsShow){
            findViewById(R.id.rl_Directionfinder).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.rl_Directionfinder).setVisibility(View.VISIBLE);
        }
        if (!leidaIsShow){
            findViewById(R.id.rl_tracker_leida).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.rl_tracker_leida).setVisibility(View.VISIBLE);
        }
        if (!trackerIsShow){
            findViewById(R.id.rl_tracker).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.rl_tracker).setVisibility(View.VISIBLE);
        }
        if (!MaoganIsShow){
            findViewById(R.id.rl_Maogan).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.rl_Maogan).setVisibility(View.VISIBLE);
        }
        if (!YcsIsShow){
            findViewById(R.id.rl_Ycs).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.rl_Ycs).setVisibility(View.VISIBLE);
        }
    }

    private void showCustomDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.device_manager_dialog, null);
        ToggleButton wireless_tog = dialogView.findViewById(R.id.wireless_tog);
        ToggleButton dirctionfinder_tog = dialogView.findViewById(R.id.dirctionfinder_tog);
        ToggleButton leida_tog = dialogView.findViewById(R.id.leida_tog);
        ToggleButton tracker_tog = dialogView.findViewById(R.id.tracker_tog);
        ToggleButton ycs_tog = dialogView.findViewById(R.id.Ycs_tog);
        ToggleButton Maogan_tog = dialogView.findViewById(R.id.Maogan_tog);

        Button exit_btn = dialogView.findViewById(R.id.exit_btn);

        AlertDialog customDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        wireless_tog.setChecked(wirelessIsShow);
        dirctionfinder_tog.setChecked(dirctionfinderIsShow);
        leida_tog.setChecked(leidaIsShow);
        tracker_tog.setChecked(trackerIsShow);
        Maogan_tog.setChecked(MaoganIsShow);
        ycs_tog.setChecked(YcsIsShow);
        exit_btn.setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
            //获取Editor对象的引用
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //将获取过来的值放入文件
            editor.putBoolean("wireless_tog", wireless_tog.isChecked());
            editor.putBoolean("dirctionfinder_tog",dirctionfinder_tog.isChecked());
            editor.putBoolean("leida_tog",leida_tog.isChecked());
            editor.putBoolean("tracker_tog",tracker_tog.isChecked());
            editor.putBoolean("Maogan_tog",Maogan_tog.isChecked());
            editor.putBoolean("Ycs_tog",ycs_tog.isChecked());
            // 提交数据
            editor.commit();
            initView();
            customDialog.dismiss();
        });
        customDialog.show();

    }

    private int clickNumb = 0;
    // region leida
    private void clickTrackerLeiDa() {
        FeedbackUtil.getInstance().doFeedback();//设置音频资源
        if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            gotoLeida();
            return;
        }
    }

    private void ycsClick (){
        FeedbackUtil.getInstance().doFeedback();//设置音频资源
        if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            startActivity((new Intent(HomeActivity.this, YcsMainActivity.class)));
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