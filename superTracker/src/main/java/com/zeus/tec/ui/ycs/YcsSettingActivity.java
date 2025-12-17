package com.zeus.tec.ui.ycs;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityYcsSettingBinding;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.ui.leida.util.INIutil;
import com.zeus.tec.ui.leida.util.IOtool;

import java.io.File;
import java.util.Properties;

public class YcsSettingActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityYcsSettingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYcsSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView ();
        initListenter();
    }

    private void initView (){

    }

    private void  initListenter() {
        binding.tvOk.setOnClickListener(this);
        binding.ivBack.setOnClickListener(this);
        binding.tvCancel.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()){
            case R.id.tv_ok:{
                writeSetting();
                ToastUtils.showShort("系统设置保存成功!");
                break;
            }
            case R.id.iv_back:
            case R.id.tv_cancel: {
                finish();
                break;
            }
        }
    }

    private class YcsSetting {
        String server_ip;
        String rec_port;
        String send_port;
        String send_outtime;
    }
    String filePath = PathUtils.getExternalAppFilesPath()+File.separator+"YcsData" + File.separator + "sys.properties";
    private void initSetting (){
        IOtool.creatFile(PathUtils.getExternalAppFilesPath()+ File.separator +"YcsData");

        if (!FileUtils.isFileExists(filePath)){
            Properties tmpProperties = new Properties();
            tmpProperties.setProperty("server_ip", "192.168.43.30");
            tmpProperties.setProperty("port", "1234");
            tmpProperties.setProperty("Local_port", "2222");
            tmpProperties.setProperty("OutTime", "1000");
            INIutil.writeproperties(tmpProperties,filePath);
        }

           if (IOtool.isFileExists(filePath)) {
               String server_ip = INIutil.readINI(filePath, "server_ip", "192.168.43.30");
               String port = INIutil.readINI(filePath, "port", "1234");
               String time = INIutil.readINI(filePath, "OutTime", "1000");
           }

    }

    private void writeSetting (){
        Properties tmpProperties = new Properties();
        String str1 = binding.edtVol.getText().toString();
        tmpProperties.setProperty("server_ip",  str1 );
        tmpProperties.setProperty("port", "1234");
        tmpProperties.setProperty("Local_port", "2222");
        tmpProperties.setProperty("OutTime", "1000");
        INIutil.writeproperties(tmpProperties,filePath,"YcsSetting");
    }
}