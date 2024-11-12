package com.zeus.tec.ui.ycs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.zeus.tec.databinding.ActivityYcsProjectSettingBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.ycs.YcsMainCache;
import com.zeus.tec.ui.leida.LeidaDataCollectActivity;
import com.zeus.tec.ui.leida.ProjectleidainfoActivity;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.INIutil;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.ui.tracker.util.TextHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

public class YcsProjectSettingActivity extends AppCompatActivity {

    ActivityYcsProjectSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityYcsProjectSettingBinding.inflate(getLayoutInflater());
        initView();
        setContentView(binding.getRoot());
        binding.ivBack.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });
        binding.tvNext.setOnClickListener(v -> {
            FeedbackUtil.getInstance().doFeedback();
            judgment();
        });
    }

    YcsMainCache cache = YcsMainCache.GetInstance();
    private final int[] sendEnergyAry = {10, 20, 40};
    private final float[] sampleTimeAry = {12.8f, 25.6f, 51.2f, 102.4f};
    String FileSavePath ="";

    private void initView() {
        binding.niceSpinnerSampleTime.attachDataSource(new LinkedList<>(Arrays.asList("12.8", "25.6", "51.2", "102.4")));
        binding.niceSpinnerSendEnergy.attachDataSource(new LinkedList<>(Arrays.asList("10", "20", "40")));
    }

    public void judgment() {
        String projectStr = binding.edtProject.getText().toString();
        if (TextHelper.isInvalidTextAndShowWarn(projectStr, "项目名称")) {
            ToastUtils.showLong("项目名称不能为空");
            return;
        }
        String edtPointDistanceString = binding.edtPointDistance.getText().toString();
        float edtPointDistance = Float.parseFloat(edtPointDistanceString);
        if (TextUtils.isEmpty(edtPointDistanceString)) {
            ToastUtils.showLong("打点距离不能为空");
            return;
        } else {
            if (edtPointDistance <= 0) {
                ToastUtils.showLong("打点距离必须大于0!");
                return;
            }
        }
        String edtRecAreaXString = binding.edtRecAreaX.getText().toString();
        float edtRecAreaX = Float.parseFloat(edtRecAreaXString);
        if (TextUtils.isEmpty(edtRecAreaXString)) {
            ToastUtils.showLong("接收面接X不能为空");
            return;
        }
        String edtRecAreaYString = binding.edtRecAreaY.getText().toString();
        float edtRecAreaY = Float.parseFloat(edtRecAreaYString);
        if (TextUtils.isEmpty(edtRecAreaYString)) {
            ToastUtils.showLong("接收面接Y不能为空");
            return;
        }
        String edtRecAreaZString = binding.edtRecAreaZ.getText().toString();
        float edtRecAreaZ = Float.parseFloat(edtRecAreaZString);
        if (TextUtils.isEmpty(edtRecAreaZString)) {
            ToastUtils.showLong("接收面接Z不能为空");
            return;
        }

        String edtSendAreaString = binding.edtSendArea.getText().toString();
        float edtSendArea = Float.parseFloat(edtSendAreaString);
        if (TextUtils.isEmpty(edtSendAreaString)) {
            ToastUtils.showLong("发射面积不能为空");
            return;
        } else {
            if (edtSendArea <= 0 || edtSendArea > 1000) {
                ToastUtils.showLong("发射面积为(1-1000)之内");
            }
        }

        String edtMarkSpaceStr = binding.edtMarkSpace.getText().toString();
        float edtMarkSpace = Float.parseFloat(edtMarkSpaceStr);
        if (TextUtils.isEmpty(edtMarkSpaceStr)) {
            ToastUtils.showLong("标记间隔不能为空");
            return;
        }

        String edtOverlayNumberStr = binding.edtOverlayNumber.getText().toString();
        int edtOverlayNumber = Integer.parseInt(edtOverlayNumberStr);
        if (TextUtils.isEmpty(edtOverlayNumberStr)) {
            ToastUtils.showLong("叠加次数不能为空");
            return;
        }
        if (edtOverlayNumber < 0 || edtOverlayNumber > 32000) {
            ToastUtils.showLong("叠加次数只能是1到32000之间");
            return;
        }

        String edtGyroStr = binding.edtGyro.getText().toString();
        if (TextUtils.isEmpty(edtGyroStr)) {
            ToastUtils.showLong("陀螺阈值不能为空");
        }
        float edtGyro = Float.parseFloat(edtGyroStr);
        if (edtGyro < 50) {
            ToastUtils.showLong("陀螺阈值必须大于50！");
            return;
        }
        //发射能量 10，20，40
        cache.sendEnergy = sendEnergyAry[binding.niceSpinnerSendEnergy.getSelectedIndex()];
        cache.sampleTime = sampleTimeAry[binding.niceSpinnerSampleTime.getSelectedIndex()];
        //采样时间 :12.8  25.6  51.2  102.4
        String tvSampleLengthStr = binding.tvSampleLength.getText().toString();
        String tvSampleIntervelStr = binding.tvSampleIntervel.getText().toString();
        String tvSendFrequencyStr = binding.tvSendFrequency.getText().toString();


        cache.projectName = projectStr;
        FileSavePath = cache.rootFilePath + File.separator + cache.projectName + File.separator + cache.projectName + ".dat";
        cache.pointDistance = edtPointDistance;
        cache.reciveAreaX = edtRecAreaX;
        cache.reciveAreaY = edtRecAreaY;
        cache.reciveAreaZ = edtRecAreaZ;
        cache.sendArea = edtSendArea;
        cache.markSpace = edtMarkSpace;
        cache.sampleCount = Integer.parseInt(tvSampleLengthStr);
        cache.sampleIntervel = Float.parseFloat(tvSampleIntervelStr);
        // cache.sendFrenquency = Float.parseFloat(tvSendFrequencyStr);
        cache.sendFrenquency = 12.5f;
        cache.overLayNumber = edtOverlayNumber;
        cache.sendEnergyIndex = binding.niceSpinnerSendEnergy.getSelectedIndex();
        cache.sampleTimeIndex = binding.niceSpinnerSampleTime.getSelectedIndex();
        cache.gyro = edtGyro;
        CreatProject_Click();
        // String publicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
// 获取当前App的私有存储路径
    }

    private void CreatProject_Click() {
        String content = "";
        content = String.valueOf(cache.pointDistance);
        String CreatTime = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            int day = now.getDayOfMonth();
            int hour = now.getHour();
            int minute = now.getMinute();
            int second = now.getSecond();
            String tmp = String.valueOf(year);
            CreatTime += tmp;
            tmp = String.valueOf(month);
            if (month < 10)
                tmp = "0" + tmp;
            CreatTime += "-";
            CreatTime += tmp;
            tmp = String.valueOf(day);
            if (day < 10)
                tmp = "0" + tmp;
            CreatTime += "-";
            CreatTime += tmp;
            tmp = String.valueOf(hour);
            if (hour < 10)
                tmp = "0" + tmp;
            CreatTime += " ";
            CreatTime += tmp;
            tmp = String.valueOf(minute);
            if (minute < 10)
                tmp = "0" + tmp;
            CreatTime += ":";
            CreatTime += tmp;
            tmp = String.valueOf(second);
            if (second < 10)
                tmp = "0" + tmp;
            CreatTime += ":";
            CreatTime += tmp;
            content += '\t';
            content += CreatTime;
        }
        content += '\t';
        content += String.valueOf(cache.reciveAreaX);
        content += '\t';
        content += String.valueOf(cache.reciveAreaY);
        content += '\t';
        // content += Amplify.ToString("f1");
        content += String.valueOf(cache.reciveAreaZ);
        content += '\t';
        // content += SampleFrequency.ToString();
        content += String.valueOf(cache.sendArea);
        content += '\t';
        // content += TimeInterval.ToString("f2");
        content += String.valueOf(cache.markSpace);
        content += '\t';
        // content += GYROThreshold.ToString("f2");
        content += String.valueOf(cache.sendFrenquency);
        content += '\t';
        content += String.valueOf(cache.sampleCount);
        content += '\t';
        content += String.valueOf(cache.sampleIntervel);
        content += '\t';
        content += String.valueOf(cache.overLayNumber);
        content += '\t';
        content += String.valueOf(sendEnergyAry[cache.sendEnergyIndex]);
        content += '\t';
        content += String.valueOf(sampleTimeAry[cache.sampleTimeIndex]);
        content += '\t';
        content += String.valueOf(cache.gyro);
        content += '\t';
        cache.creatTime = CreatTime;
        //int AmplifyValue = GetAmp(info.Amp1);
        int a = 0;
        int result = cache.GetSetting(cache.projectName, cache.reciveAreaX, cache.reciveAreaY,
                cache.reciveAreaZ, cache.sendArea, cache.markSpace, cache.sampleCount, cache.sampleIntervel
                , cache.sendFrenquency, cache.overLayNumber, cache.sendEnergyIndex, cache.sampleTimeIndex, cache.gyro);
        if (result == 1)
            //测试

            if (true) {
                String dataFilePath = PathUtils.getExternalAppFilesPath() + File.separator + "YcsData" + File.separator + cache.projectName;
                if (!FileUtils.createOrExistsDir(dataFilePath)) {
                    LogUtils.e("创建文件失败：" + dataFilePath);
                    ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                    return;
                }
                String datapath = dataFilePath + File.separator + cache.projectName + ".trd";

                String finalContent = content;
                if (IOtool.isFileExists(datapath)) {
                    MesseagWindows.showMessageBox(YcsProjectSettingActivity.this, "是否覆盖原项目", "该项目已存在，是否覆盖原项目", new DialogCallback() {

                        @Override
                        public void onPositiveButtonClick() {
                            saveData(FileSavePath.replace(".dat", ".trd"), finalContent, cache);
                        }

                        @Override
                        public void onNegativeButtonClick() {
                            startActivity(new Intent(YcsProjectSettingActivity.this, YcsDataCollectActivity.class));
                        }
                    });
                } else {
                    saveData(FileSavePath.replace(".dat", ".trd"), finalContent, cache);
                    if (!cache.RefreshInitFile()) {
                        ToastUtils.showLong("配置文件无法访问");
                    }
                    ToastUtils.showLong("设备项目创建成功");
                }
            } else if (false) {
                // Toast.makeText(this,"设备通讯数失败",Toast.LENGTH_SHORT);
                ToastUtils.showLong("设备通讯失败");
            }
    }

    private void saveData(String dataPath, String finalContent, YcsMainCache cache) {
        if (!FileUtils.createOrExistsFile(dataPath)) {
            LogUtils.e("创建文件失败：" + cache.rootFilePath);
            ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
            return;
        }

        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<leida_info>() {
            @Override
            public leida_info doInBackground() throws Throwable {
                try {
                    IOtool.saveText(dataPath, finalContent);
                    cache.properties.setProperty("lastProject", cache.projectName);
                    INIutil.writeproperties(cache.properties, cache.rootFilePath + File.separator + "sys.properties");
                    cache.RefreshInitFile();
                    cache.newProject += 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showLong("保存信息失败，请重试!");
                }
                return new leida_info();
            }

            @Override
            public void onSuccess(leida_info result) {
                //  hideLoading();
                if (result != null) {
                    cache.pointList.clear();
                    cache.FileSavePath =FileSavePath;
                    cache.trdFilePath = cache.rootFilePath + File.separator + cache.projectName + File.separator + cache.projectName + ".trd";
                    ActivityUtils.finishActivity(YcsDataCollectActivity.class);
                    startActivity(new Intent(YcsProjectSettingActivity.this, YcsDataCollectActivity.class));
                    finish();
                }
            }
        });
    }


}