package com.zeus.tec.ui.ycs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
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
    }

    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()){
            case R.id.tv_ok:{
               // writeSetting();
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
        tmpProperties.setProperty("server_ip", "192.168.43.30");
        tmpProperties.setProperty("port", "1234");
        tmpProperties.setProperty("Local_port", "2222");
        tmpProperties.setProperty("OutTime", "1000");
        INIutil.writeproperties(tmpProperties,filePath,"YcsSetting");
    }
}