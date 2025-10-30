package com.zeus.tec.ui.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityVirtualCollectBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.device.tracker.TrackerDataManager;
import com.zeus.tec.model.tracker.CollectTimeInfo;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.model.tracker.PointRecordInfo;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.ui.tracker.adapter.TrackerPointRecordAdapter;
import com.zeus.tec.ui.tracker.util.ProjectInfoManager;
import com.zeus.tec.ui.tracker.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Condition;

public class VirtualCollectActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityVirtualCollectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_virtual_collect);
        binding = ActivityVirtualCollectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUi();
        initListener();
        context = this;
        tvResult = binding.tvResult;

    }

    List<PointRecordInfo> pointRecordInfoList = new ArrayList<>();
    List<CollectTimeInfo> collectTimeInfoList = new ArrayList<>();
    Context context;

    private void initListener() {
        binding.btnAddDateTime.setOnClickListener(this);
        binding.addVirtualData.setOnClickListener(this);
        binding.saveVirtualData.setOnClickListener(this);
    }

    private void initUi() {
        TrackerPointRecordAdapter trackerPointRecordAdapter = new TrackerPointRecordAdapter(context, pointRecordInfoList);
        binding.virtualDataList.setAdapter(trackerPointRecordAdapter);
    }

    private TextView tvResult;
    private final ArrayList<String> dateTimeList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Step 1️⃣ 弹出日期选择器
     **/
    private void showDatePicker() {
        try {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("选择日期")
                    .setTheme(R.style.ThemeOverlay_Material3_MaterialCalendar)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                String dateStr = dateFormat.format(calendar.getTime());
                showFullTimePicker(dateStr); // 日期选完后弹时间
            });
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        } catch (Exception exception) {
            int a = 1;
        }
    }

    private long getGMTtimestampMillis(String dateStr)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
// 解析为本地时间
            var localDateTime = LocalDateTime.parse(dateStr, formatter);
// 转换为 GMT（UTC）时区
            var gmtZoned = localDateTime.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("GMT"));
// 转换为时间戳（毫秒）
            return gmtZoned.toInstant().toEpochMilli();
        }
        else {
            return 0;
        }
    }

    /**
     * Step 2️⃣ 弹出自定义时分秒对话框
     **/
    private void showFullTimePicker(String dateStr) {
        try {
            View view = getLayoutInflater().inflate(R.layout.dialog_time_picker_full, null);
            NumberPicker npHour = view.findViewById(R.id.npHour);
            NumberPicker npMinute = view.findViewById(R.id.npMinute);
            NumberPicker npSecond = view.findViewById(R.id.npSecond);
            // 初始化范围
            npHour.setMinValue(0);
            npHour.setMaxValue(23);
            npMinute.setMinValue(0);
            npMinute.setMaxValue(59);
            npSecond.setMinValue(0);
            npSecond.setMaxValue(59);
            // 默认值
            npHour.setValue(lastHour);
            npMinute.setValue(lastMinute);
            npSecond.setValue(lastSecond);
            new AlertDialog.Builder(this)
                    .setTitle("选择时间（时:分:秒）")
                    .setView(view)
                    .setPositiveButton("确定", (dialog, which) -> {
                        lastHour = npHour.getValue();
                        lastMinute = npMinute.getValue();
                        lastSecond = npSecond.getValue();
                        String formatted = String.format(Locale.getDefault(),
                                "%02d:%02d:%02d", lastHour, lastMinute, lastSecond);
                        dateTimeList.add(dateStr + " " + formatted);
                        updateResultText();
                        showRepeatDialog(); // 是否继续输入
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            int a = 1;
        }
    }

    private int lastHour =12;
    private int lastMinute =0;
    private int lastSecond =0;

    /**
     * Step 3️⃣ 更新结果文本
     **/
    private void updateResultText() {
        StringBuilder sb = new StringBuilder();

        tvResult.setText(dateTimeList.get(dateTimeList.size()-1));
    }

    /**
     * Step 4️⃣ 可选：自动再次打开日期选择
     **/
    private void showRepeatDialog() {
        // 如果想在用户选择完自动再选下一组，直接调用：
        // showDatePicker();
    }

    DrillHoleInfo drillHoleInfo = ProjectInfoManager.getInstance().getOrNewDrillHoleInfo();

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()) {
            case R.id.add_virtual_data: {
                PointRecordInfo pointRecordInfoTmp = new PointRecordInfo();
                pointRecordInfoTmp.directionAngle = Float.parseFloat(String.valueOf(binding.directionAngleEdt.getText()));
                pointRecordInfoTmp.slantAngle = Float.parseFloat(String.valueOf(binding.slantAngleEdt.getText()));
                pointRecordInfoTmp.rollAngle = Float.parseFloat(String.valueOf(binding.rollAngleEdt.getText()));
                pointRecordInfoTmp.collectTime = (String) binding.tvResult.getText();
                pointRecordInfoList.add(pointRecordInfoTmp);
                refreshAdapter();
                break;
            }
            case R.id.btnAddDateTime: {
                showDatePicker();
                break;
            }
            case R.id.save_virtual_data:{
                MesseagWindows.showMessageBox(this, "数据保存", "是否保存当前数据点", new DialogCallback() {
                    @Override
                    public void onPositiveButtonClick() {
                        if (pointRecordInfoList.size()>0){
                            for (int i = 0; i < pointRecordInfoList.size(); i++) {
                              collectTimeInfoList.add(addCollectTimeInfo(pointRecordInfoList.get(i)));
                            }
                            saveMergeData(drillHoleInfo,collectTimeInfoList);
                            doShowData(drillHoleInfo);
                        }
                    }
                    @Override
                    public void onNegativeButtonClick() {

                    }
                });
            }
        }
    }
    private void doShowData(DrillHoleInfo info) {
        if (info == null) return;
        DataCollectActivity.launch(this, info.id, 2);
        finish();
    }

    private void saveMergeData(DrillHoleInfo oneDrillHoleInfo,List<CollectTimeInfo> timeList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        oneDrillHoleInfo.collectCount = pointRecordInfoList.size();
        oneDrillHoleInfo.isMerged= true;
        String fileName = sdf.format(new Date(oneDrillHoleInfo.collectionDateTime)) + ".dat";
        String dataPath = oneDrillHoleInfo.projectRoot + File.separator + fileName;
        TrackerDataManager.writeTrackerInfoToFile(dataPath, oneDrillHoleInfo, timeList);
        oneDrillHoleInfo.dataPath = dataPath;
        TrackerDBManager.saveOrUpdate(oneDrillHoleInfo);

        List<String> fileList = new ArrayList<>();
        fileList.add(oneDrillHoleInfo.livePhotos);
        fileList.add(oneDrillHoleInfo.dataPath);
        String zipFile = oneDrillHoleInfo.projectRoot + File.separator + sdf.format(new Date(oneDrillHoleInfo.collectionDateTime)) + ".zip";
        try {
            ZipUtils.zipFiles(fileList, zipFile);
            oneDrillHoleInfo.zipPath = zipFile;
            TrackerDBManager.saveOrUpdate(oneDrillHoleInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CollectTimeInfo addCollectTimeInfo(PointRecordInfo pointRecordInfo_one) {
        CollectTimeInfo collectTimeInfoTmp = new CollectTimeInfo();
        collectTimeInfoTmp.drillInfoId = drillHoleInfo.id;
        collectTimeInfoTmp.slantAngle = (short) (pointRecordInfo_one.slantAngle * 100);
        collectTimeInfoTmp.omega = (short) (pointRecordInfo_one.slantAngle * 100);
        collectTimeInfoTmp.rollAngle = (short) (pointRecordInfo_one.rollAngle * 100);
        collectTimeInfoTmp.directionAngle = (short) (pointRecordInfo_one.directionAngle * 100);
        collectTimeInfoTmp.collectTime =8*3600+ getGMTtimestampMillis( pointRecordInfo_one.collectTime)/1000;
        TrackerDBManager.savOrUpdate(collectTimeInfoTmp);
        return collectTimeInfoTmp;
    }

    private void refreshAdapter() {
        TrackerPointRecordAdapter trackerPointRecordAdapter = new TrackerPointRecordAdapter(context, pointRecordInfoList);
        binding.virtualDataList.setAdapter(trackerPointRecordAdapter);
        binding.virtualDataList.setSelection(trackerPointRecordAdapter.getCount() - 1);
    }
}