package com.zeus.tec.ui.tracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityDataCollectBinding;
import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.device.tracker.CmdManager;
import com.zeus.tec.device.tracker.CompassData;
import com.zeus.tec.device.tracker.TrackerCollectData;
import com.zeus.tec.device.tracker.TrackerDataManager;
import com.zeus.tec.device.usbserial.ParserCenter;
import com.zeus.tec.device.usbserial.USBSerialManager;
import com.zeus.tec.event.MergeEvent;
import com.zeus.tec.event.TrackerCollectDataEvent;
import com.zeus.tec.event.TrackerDataSizeEvent;
import com.zeus.tec.event.WireCollectDataEvent;
import com.zeus.tec.model.tracker.CollectTimeInfo;
import com.zeus.tec.model.tracker.DrillDataInfo;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.tracker.config.SystemConfig;
import com.zeus.tec.ui.tracker.util.TimeUtil;
import com.zeus.tec.ui.widget.chart.MyMarkerView;
import com.zeus.tec.ui.widget.chart.ZeusRender;
import com.zeus.tec.model.utils.DataUtils;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DataCollectActivity extends BaseActivity implements USBSerialManager.USBStateChangeListener, Handler.Callback {
    private static final String TAG = "zeus_collect";
    private int collectDataStatus = 0; // 0 初始阶段 1 有线数据阶段 2 无线采集阶段 100 数据合成阶段 200 数据展示
    private SuperLogUtil superLogUtil;
    private ActivityDataCollectBinding binding;
    private Handler h = new Handler(Looper.getMainLooper(),this);
    private static final String KEY_DETECT_HEALTH_01 = "DETECT_HEALTH_01";
    private static final String KEY_DETECT_HEALTH_02 = "DETECT_HEALTH_02";
    private static final String KEY_STOP_WIRE_COLLECT = "STOP_WIRE_COLLECT";
    private static final String KEY_EXIT_WIRE_MODE = "EXIT_WIRE_MODE";
    private static final String KEY_GET_DATA_FRAME_SIZE = "KEY_GET_DATA_FRAME_SIZE";
    private static final String KEY_GET_DATA = "KEY_GET_DATA";
    private static final String KEY_DATA_BUMP = "KEY_DATA_BUMP";
    private static final String KEY_START_WIRE_COOLECT = "KEY_START_WIRE_COOLECT";

    private final int MSG_DETECT_HEALTH_01 = 1;
    private final int MSG_DETECT_HEALTH_02 = 2;
    private static final int MSG_RESET_UI = 3;
    private static final int MSG_DETECT_BUMP = 4;
    private static final int MSG_STOP_WireCollectData = 5;
    private static final int MSG_EXIT_WireMode = 6;
    private static final int MSG_CHECK_TIME = 7;
    private static final int MSG_GET_DATA = 8;
    private static final int MSG_GET_DATA_FRAME_SIZE = 9;
    private static final int MSG_DATA_BUMP = 10;
    private static final int MSG_GET_DATA_SIZE_BUMP = 11;
    private static final int MSG_START_WIRE_COLLECT = 12;
    private static final int MSG_START_WIRELESS_COLLECT = 13;
    private HashMap<String, String> map = new HashMap<>();
    private long countId = 1;

    private List<CollectTimeInfo> timeList = new ArrayList<>();
    private DrillHoleInfo oneDrillHoleInfo = null;

    public static final String KEY_DRILL_INFO_ID = "DRILL_INFO_ID";
    public static final String KEY_TYPE_MERGE = "TYPE_MERGE";

    private int mergeStart = 0;
    boolean isGettingData;
    private List<TrackerCollectData> dataList = new LinkedList<>();
    private long recvCount = 0;
    private int curIdx = -1;
    private boolean hasSendStartWireCollect;
    private long collectCount = 0;
    private long lastClick = 0;
    private long dataSize;

    private long drillId = 0;
    private int funType = 0;

    private float volThreashHold = SystemConfig.getVolThreshHold();

    public static void launch(Context context, long drillInfoId) {
        launch(context, drillInfoId, 0);
    }

    public static void launch(Context context, long drillInfoId, int type) {
        Intent intent = new Intent(context, DataCollectActivity.class);
        intent.putExtra(KEY_DRILL_INFO_ID, drillInfoId);
        intent.putExtra(KEY_TYPE_MERGE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drillId = getIntent().getLongExtra(KEY_DRILL_INFO_ID, 0);
        funType = getIntent().getIntExtra(KEY_TYPE_MERGE, 0);
        if (drillId != 0) {
            oneDrillHoleInfo = TrackerDBManager.getOneDrillHoleInfo(drillId);
        }
        if (BuildConfig.DEBUG) {
            LogUtils.d(drillId, oneDrillHoleInfo);
        }

        if (oneDrillHoleInfo == null) {
            ToastUtils.showLong("错误工程信息，请重试");
            finish();
            return;
        }

        superLogUtil = new SuperLogUtil(this);
        EventBus.getDefault().register(this);
        initUI();
    }


    private void initUI() {
        collectDataStatus = 0;
        if (funType == 1) {
            collectDataStatus = 100;
        } else if( funType == 2) {
            collectDataStatus = 200;
        }

        binding = ActivityDataCollectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (oneDrillHoleInfo != null) {
            // 设计方位
            binding.tv21.setText("" + oneDrillHoleInfo.designDirection);
            // 设计倾角
            binding.tv22.setText("" + oneDrillHoleInfo.designAngle);
        }

        binding.ivBack.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            onClickBack();
        });
        binding.tvCollect.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            collectData();
        });
        binding.tvFinishCollect.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finishCollect();
        });
        binding.tvMerge.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            mergeData();
        });

        binding.tvStep25Text.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            enterWirelessMode();
        });

        binding.tvStep3Text.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            clearData();
        });

        binding.tvStep4Text.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            startCollectData();
        });

        binding.tvNext.setOnClickListener( v-> {
//            CollectTimeInfo ti = timeList.get(curIdx);
//            binding.chart1.highlightValue(e.getX(), 0);
        });

        if( funType != 0) {
            showMergeUI();
            loadCollectData();
        } else {
            step_1();
        }

        USBSerialManager usbSerialManager = USBSerialManager.getInstance();
        usbSerialManager.addStateChangedListener(this);
        usbSerialManager.refresh(this);

        if (BuildConfig.DEBUG) {
            binding.tvShowDebug.setVisibility(View.VISIBLE);
            binding.tvShowDebug.setOnClickListener( v->{
                FeedbackUtil.getInstance().doFeedback();
                superLogUtil.show();
            });
            /*binding.tvGetData.setVisibility(View.VISIBLE);
            binding.tvGetData.setOnClickListener( v->{
                mergeData();
            });*/
        }
    }

    private void enterWirelessMode() {
        clearMsg();
        stopWireCollectData();
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int dataCnt = 0;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvCompassData(WireCollectDataEvent event) {
        if (event == null) return;
        CompassData compassData = event.compassData;
        if (compassData == null) return;

        if (collectDataStatus != 1) return;

        binding.tvCountTime.setText(sdf.format(new Date(compassData.time)));
        binding.tvCountTime.setTextSize(15);
        binding.tvCountTimeLabel.setText("时间");

        // 横滚角
        binding.tv11.setText(String.format("%.2f",compassData.rollAngle/100f));
        // 俯仰角
        binding.tv12.setText(String.format("%.2f",compassData.omega/100f));
        // 倾角值
        binding.tv13.setText(String.format("%.2f",compassData.slantAngle/100f));
        // 方位角
        binding.tv14.setText(String.format("%.2f",compassData.directionAngle/100f));

        // 电压
        binding.llLowPowerTip.setVisibility(View.VISIBLE);
        if (compassData.voltage < volThreashHold ) {
            binding.tvPower.setTextColor(Color.RED);
            binding.ivPower.setColorFilter(Color.RED);
            binding.tvPower.setText("探头电量过低(电池电压: " + compassData.voltage + " v)");
        } else {
            binding.tvPower.setTextColor(Color.GREEN);
            binding.ivPower.setColorFilter(Color.GREEN);
            binding.tvPower.setText("电池电压: " + compassData.voltage + " v");
        }

        dataCnt++;

        if( dataCnt > 0) {
            //stopWireCollectData();
        }

        step_25();
    }



    private void finishCollect() {
        if (BuildConfig.DEBUG)  superLogUtil.d( "结束采集...");

        if (collectCount <= 0 ) {
            return;
        }
        binding.tvCountTime.stop();
        oneDrillHoleInfo.countTimeTotal = binding.tvCountTime.getCountTime();
        oneDrillHoleInfo.collectCount = collectCount;
        TrackerDBManager.saveOrUpdate(oneDrillHoleInfo);


        binding.tvStatus.setText("已停止");
        binding.ivStep4.setState(2);
        binding.tvStep4Text.setText("数据采集已完成");
        binding.tvFinishCollect.setVisibility(View.GONE);

        binding.tvCollect.setVisibility(View.GONE);

        binding.tvMerge.setVisibility(View.VISIBLE);

        EventBus.getDefault().post(new MergeEvent(oneDrillHoleInfo));
    }

    private void collectData() {
        long now = System.currentTimeMillis();
        if (now - lastClick < 1000) return;

        lastClick = now;
        collectCount++;
        binding.tv24.setText(String.valueOf(collectCount));
        superLogUtil.d( "采集数据: " + collectCount);
        if (oneDrillHoleInfo != null) {
            binding.tv23.setText(String.format("%.2f", oneDrillHoleInfo.drillPipeLength * collectCount / 100f));

            CollectTimeInfo collectTimeInfo = new CollectTimeInfo();
            collectTimeInfo.time = TimeUtil.getGMTTime();
            collectTimeInfo.drillInfoId = oneDrillHoleInfo.id;
            collectTimeInfo = TrackerDBManager.savOrUpdate(collectTimeInfo);
            timeList.add(collectTimeInfo);
        } else {
            throw new RuntimeException("请先设置项目信息");
        }
    }


    @Override
    public void onBackPressed() {
        onClickBack();
    }

    private void onClickBack() {
        finish();
    }


    private void step_1() {
        if (BuildConfig.DEBUG ) superLogUtil.d( "准备连接探头");
        binding.ivStep1.setState(1);
    }

    private void step_2() {
        if (BuildConfig.DEBUG) superLogUtil.d( "开始检测探头健康状态");

        binding.ivStep1.setState(2);
        binding.tvStep1Text.setText("探头已连接");
        binding.ivLine1.setBackgroundColor(Color.parseColor("#0E65EE"));

        binding.ivStep2.setState(1);
        binding.tvStep2Text.setTextColor(Color.parseColor("#222222"));

        detectHealth();
    }

    private void step_25() {
        superLogUtil.d("step25...." + binding.ivStep25.getState());
        if (binding.ivStep25.getState() != 0) return;
        clearMsg();

        binding.tvStatus.setText("运行中");
        binding.tvStatus.setTextColor(Color.WHITE);

        binding.ivStep2.setState(2);
        binding.tvStep2Text.setText("探头工作正常(有线模式中)");
        binding.ivLine2.setBackgroundColor(Color.parseColor("#0E65EE"));

        binding.ivStep25.setState(1);
        binding.tvStep25Text.setEnabled(true);
    }

    private void stopWireCollectData() {
        clearMsg();
        h.sendEmptyMessage(MSG_STOP_WireCollectData);
    }

    private void doStopWireCollectData() {
        if (BuildConfig.DEBUG) superLogUtil.d("03: 发送 停止有线采集命令");

        binding.tvStep25Text.setEnabled(false);

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_STOP_WIRE_COLLECT, id);
        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.stopCollectFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_STOP_WIRE_COLLECT) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("03: 停止有线采集命令 执行成功");

                Message msg = Message.obtain();
                msg.what = MSG_EXIT_WireMode;
                msg.arg1 = 1;
                h.sendMessageDelayed(msg, 2000);
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_STOP_WIRE_COLLECT) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("03: 停止有线采集命令 执行失败：" + error);

                ToastUtils.showLong("进入无线模式失败，请重试！");
                binding.tvStep25Text.setEnabled(true);
                //h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_01, 2000);
            }
        }));
    }

    private void doExitWireCollectMode(boolean shouldGoToNext) {
        if (BuildConfig.DEBUG) superLogUtil.d("04: 发送 退出有线模式命令");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_EXIT_WIRE_MODE, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.exitWireModeFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_EXIT_WIRE_MODE) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("04: 退出有线模式命令 执行成功");

                if (shouldGoToNext) {
                    step_3();
                } else {
                    //h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_01, 5000);
                    ToastUtils.showLong("进入无线模式失败，请重试！");
                    binding.tvStep25Text.setEnabled(true);
                }
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_EXIT_WIRE_MODE) != id) return;
                if (BuildConfig.DEBUG) superLogUtil.d("04: 退出有线模式命令 执行失败:"+error);
                ToastUtils.showLong("进入无线模式失败，请重试！");
                binding.tvStep25Text.setEnabled(true);
            }
        }));
    }

    private void step_3() {
        if (BuildConfig.DEBUG) superLogUtil.d( "成功进入无线模式");
        binding.tvCountTime.setText("00:00:00");
        binding.tvCountTime.setTextSize(22);
        binding.tvCountTimeLabel.setText("运行时间");
        binding.ivStep25.setState(2);
        binding.tvStep25Text.setEnabled(false);
        binding.tvStep25Text.setTextColor(Color.parseColor("#222222"));
        binding.ivLine25.setBackgroundColor(Color.parseColor("#0E65EE"));
        clearMsg();
        binding.ivStep3.setState(1);
        binding.tvStep3Text.setEnabled(true);
    }

    private void step_4() {
        if (BuildConfig.DEBUG) superLogUtil.d( "探头数据已清空");
        hideLoading();
        binding.ivStep3.setState(2);
        binding.tvStep3Text.setEnabled(false);
        binding.tvStep3Text.setBackgroundColor(Color.TRANSPARENT);
        binding.tvStep3Text.setTextColor(Color.parseColor("#222222"));
        binding.tvStep3Text.setText("探头数据已清空");
        binding.ivLine3.setBackgroundColor(Color.parseColor("#0E65EE"));
        binding.ivStep4.setState(1);
        binding.tvStep4Text.setEnabled(true);
    }

    private void step_5() {
        if (BuildConfig.DEBUG) superLogUtil.d("01: 进入无线数据采集中");

        binding.ivLine3.setBackgroundColor(Color.parseColor("#0E65EE"));

        binding.tvStep4Text.setEnabled(false);
        binding.tvStep4Text.setBackgroundColor(Color.TRANSPARENT);
        binding.tvStep4Text.setTextColor(Color.parseColor("#222222"));
        binding.tvStep4Text.setText("数据采集中...");
        binding.tvStatus.setText("运行中");
        binding.tvStatus.setTextColor(Color.WHITE);
        binding.tvFinishCollect.setEnabled(true);
        binding.tvCollect.setEnabled(true);

        //oneDrillHoleInfo.collectionDateTime = System.currentTimeMillis();
        TrackerDBManager.saveOrUpdate(oneDrillHoleInfo);

        binding.tvCountTime.start();

        collectDataStatus = 2;
    }

    private void clearMsg() {
        h.removeCallbacksAndMessages(null);
    }

    private void detectHealth() {
        detectHealth(0);
    }

    private void detectHealth(long delay) {
        clearMsg();
        h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_01, delay);
    }

    private void detectHealth_01() {
        if (BuildConfig.DEBUG) superLogUtil.d("01: 发送 进入有线模式命令");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_DETECT_HEALTH_01, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.enterWireModeFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_DETECT_HEALTH_01) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("01: 进入有线模式命令 执行成功");

                //h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_02, 5*1000);
                clearMsg();
                h.sendEmptyMessageDelayed(MSG_START_WIRE_COLLECT, 2*1000);
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_DETECT_HEALTH_01) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("01: 进入有线模式命令 执行失败: " + error);

                clearMsg();
                h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_01, 2000);
            }
        }));
    }
    private void detectHealth_02() {
        if (BuildConfig.DEBUG) superLogUtil.d("02: 发送 设置阈值命令");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_DETECT_HEALTH_02, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.setAngleSpeedThresholdFrame(120), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_DETECT_HEALTH_02) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 设置阈值命令 执行成功");

                h.sendEmptyMessageDelayed(MSG_START_WIRE_COLLECT, 3*1000);
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_DETECT_HEALTH_02) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 设置阈值命令 执行失败: "+ error);

                h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_01, 2000);
            }
        }));
    }
    //time
    private void doStartWireCollectData() {
        if (BuildConfig.DEBUG) superLogUtil.d("02: 发送 开始有线采集命令");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_START_WIRE_COOLECT, id);

        hasSendStartWireCollect = false;
        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.startCollectFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_START_WIRE_COOLECT) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 开始有线采集命令 执行成功");

                collectDataStatus = 1;
                //h.sendEmptyMessageDelayed(MSG_DETECT_BUMP, 15*1000);
                hasSendStartWireCollect = true;
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_START_WIRE_COOLECT) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 开始有线采集命令 执行失败: " + error);

                h.sendEmptyMessageDelayed(MSG_DETECT_HEALTH_01, 2000);
            }
        }));
    }


    private void clearData() {
        if (BuildConfig.DEBUG) superLogUtil.d("03 发送 清除探头数据");

        binding.tvStep3Text.setEnabled(false);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.clearDataFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (BuildConfig.DEBUG) superLogUtil.d("03 清除探头数据 成功");

                runOnUiThread(DataCollectActivity.this::step_4);
            }

            @Override
            public void onFail(String error) {
                if (BuildConfig.DEBUG) superLogUtil.d("03 清除探头数据 失败:"+error);

                runOnUiThread(DataCollectActivity.super::hideLoading);
                binding.tvStep3Text.setEnabled(true);
            }
        }));
    }

    private void startCollectData() {
        if (BuildConfig.DEBUG) superLogUtil.d("通知探头开始无线采集");
        h.sendEmptyMessageDelayed(MSG_CHECK_TIME, 0);
    }

    private void startWirelessCollect() {
        if (BuildConfig.DEBUG) superLogUtil.d("05 发送 开始无线采集命令");

        USBSerialManager.getInstance()
                .sendFrame(new ParserCenter.FrameWrapper(CmdManager.startWirelessCollect(), new ParserCenter.SendCallback(){
            @Override
            public void onSuccess(byte[] data) {
                if (BuildConfig.DEBUG) superLogUtil.d("05 开始无线采集命令 成功");
                runOnUiThread(DataCollectActivity.this::step_5);
            }

            @Override
            public void onFail(String error) {
                if (BuildConfig.DEBUG) superLogUtil.d("05 开始无线采集命令 失败:"+error);
                binding.tvStep4Text.setEnabled(true);
            }
        }));
    }
    private void doCheckTime() {
        if (BuildConfig.DEBUG) superLogUtil.d("04 发送 对时命令");
        binding.tvStep4Text.setEnabled(false);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.setTimeFrame(System.currentTimeMillis()), new ParserCenter.SendCallback(){
            @Override
            public void onSuccess(byte[] data) {
                if (BuildConfig.DEBUG) superLogUtil.d("04 对时命令 成功");
                h.sendEmptyMessageDelayed(MSG_START_WIRELESS_COLLECT, 2000);
            }

            @Override
            public void onFail(String error) {
                if (BuildConfig.DEBUG) superLogUtil.d("04 对时命令 失败:"+error);
                detectHealth(2000);
            }
        }));
    }


    @Override
    public void onDeviceAttached() {
        if (BuildConfig.DEBUG) superLogUtil.d( "检测到USB设备");

        USBSerialManager.getInstance().start(this);
    }

    @Override
    public void onConnected() {
        if (BuildConfig.DEBUG) superLogUtil.d( "USB设备已连接");
        if (collectDataStatus < 2) {
            step_2();
        } else if( (collectDataStatus == 100 || funType == 1) && (timeList != null && timeList.size()>0) ) {
            mergeData();
        }
    }

    @Override
    public void onDisconnected() {
        if (BuildConfig.DEBUG) superLogUtil.d( "USB设备已断开");
        if (BuildConfig.DEBUG) superLogUtil.d( "collectDataStatus: " + collectDataStatus);
        h.removeCallbacksAndMessages(null);
        if( funType == 0) {
            h.sendEmptyMessage(MSG_RESET_UI);
        } else {
            ToastUtils.showLong("USB设备已断开");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new MergeEvent(oneDrillHoleInfo));
        h.removeCallbacksAndMessages(null);
        USBSerialManager.getInstance().removeStateChangedListener(this);
        USBSerialManager.getInstance().release(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_DETECT_HEALTH_01:
                detectHealth_01();
                break;
            case MSG_STOP_WireCollectData:
                doStopWireCollectData();
                break;
            case MSG_EXIT_WireMode:
                doExitWireCollectMode(msg.arg1 == 1);
                break;
            case MSG_CHECK_TIME:
                doCheckTime();
                break;
            case MSG_DETECT_HEALTH_02:
                detectHealth_02();
                break;
            case MSG_RESET_UI:
                resetUI();
                break;
            case MSG_DETECT_BUMP:
                bump();
                break;
            case MSG_GET_DATA_FRAME_SIZE:
                doGetDataSize();
                break;
            case MSG_GET_DATA:
                doGetData();
                break;
            case MSG_GET_DATA_SIZE_BUMP:
                doGetDataSizeBump();
                break;
            case MSG_START_WIRE_COLLECT:
                doStartWireCollectData();
                break;
            case MSG_START_WIRELESS_COLLECT:
                startWirelessCollect();
                break;
            case MSG_DATA_BUMP:
                doGetDataBump();
                break;
        }
        return false;
    }

    private void bump() {
        if (BuildConfig.DEBUG) superLogUtil.d( "没有收到有线采集数据....");
        h.sendEmptyMessageDelayed(MSG_EXIT_WireMode, 2000);
    }

    private void resetUI() {
        dataCnt = 0;
        hideLoading();
        USBSerialManager.getInstance().release(this);
        if (collectDataStatus < 2) {
            initUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackerDataSizeEvent(TrackerDataSizeEvent event) {
        if (event == null) return;
        dataSize = event.size;
        if (BuildConfig.DEBUG) {
            superLogUtil.d("接收到探头数组: " + dataSize);
            ToastUtils.showLong("接收到探头数组: " + dataSize);
        }
        if (dataSize > 0 ) {
            getData();
        } else {
            ToastUtils.showLong("当前探头中没有无线采集数据，请确认探头是否正确！");
        }
        h.removeMessages(MSG_GET_DATA_SIZE_BUMP);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvCollectData(TrackerCollectDataEvent event) {
        if (event == null) return;
        TrackerCollectData trackerCollectData = event.data;
        if (trackerCollectData == null) return;

        if (recvCount == 0) mergeStart = 0;

        recvCount++;
        if (mergeStart < timeList.size() ) {
            CollectTimeInfo ti = timeList.get(mergeStart);
            long diff = Math.abs(ti.time - trackerCollectData.collectTime);
            if (diff <= ti.diffTime) {
                ti.diffTime = diff;
                ti.copyData(trackerCollectData);
            } else {
                TrackerDBManager.savOrUpdate(ti);
                mergeStart++;
            }
        }
        if (oneDrillHoleInfo.isMerged) return;

        CollectTimeInfo ti = timeList.get(Math.min(mergeStart, timeList.size()-1));
        TrackerDBManager.savOrUpdate(ti);

        TrackerDBManager.saveOrUpdate(DrillDataInfo.newDrillDataInfo(trackerCollectData, oneDrillHoleInfo.id));

        if (recvCount == dataSize || mergeStart > timeList.size()) {
            h.removeMessages(MSG_DATA_BUMP);
            if (BuildConfig.DEBUG) superLogUtil.d("接收到探头数据总数: " + recvCount);
            oneDrillHoleInfo.isMerged = true;
            TrackerDBManager.saveOrUpdate(oneDrillHoleInfo);
            EventBus.getDefault().post(new MergeEvent(oneDrillHoleInfo));
            showChartWithData(timeList);
            binding.llBtns.setVisibility(View.GONE);
            saveMergeData();
        }
    }

    private void saveMergeData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

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

    private void doGetDataSize() {
        if (BuildConfig.DEBUG) superLogUtil.d("01: 发送 获取探头中存储的数据组数");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_GET_DATA_FRAME_SIZE, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.getDataFrameSize(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_GET_DATA_FRAME_SIZE) != id) return;
                if (BuildConfig.DEBUG) superLogUtil.d("01: 获取探头中存储的数据组数 执行成功");
                h.sendEmptyMessageDelayed(MSG_GET_DATA_SIZE_BUMP, 3*1000);
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_GET_DATA_FRAME_SIZE) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("01: 获取探头中存储的数据组数 执行失败:"+error);

                ToastUtils.showLong("获取探头无线采集数据失败，请重试!");

                if (funType == 1) {
                    showRetryMergeBtn();
                }
            }
        }));
    }

    private void showRetryMergeBtn() {
        binding.llBtns.setVisibility(View.VISIBLE);
        binding.tvFinishCollect.setVisibility(View.GONE);
        binding.tvCollect.setVisibility(View.GONE);
        binding.tvMerge.setVisibility(View.VISIBLE);
        clearMsg();
    }

    private void doGetDataSizeBump() {
        if (BuildConfig.DEBUG) superLogUtil.d("01: 超时炸弹，没有接收到探头中存储的数据组数");

        if (funType == 1) {
            showRetryMergeBtn();
        }
    }
    private void doGetDataBump() {
        if (funType == 1) {
            showRetryMergeBtn();
        }

        if (BuildConfig.DEBUG) superLogUtil.d("01: 超时炸弹，没有完全接收到探头中存储的数据，当前接收数据数："+
                recvCount+",实际数量：" + dataSize);
    }

    private void getData() {
        h.removeMessages(MSG_GET_DATA);
        h.sendEmptyMessageDelayed(MSG_GET_DATA, 500);
    }

    private void doGetData() {
        if (BuildConfig.DEBUG) superLogUtil.d("02: 发送 启动上传探头中存储的数据");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_GET_DATA, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.startUploadDataFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(MSG_DATA_BUMP) != id) return;

                isGettingData = true;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 启动上传探头中存储的数据 执行成功");

                setDataBump();
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_GET_DATA) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 启动上传探头中存储的数据 执行失败: "+ error);

                isGettingData = false;
                ToastUtils.showLong("获取探头无线采集数据失败，请重试!");

                if (funType == 1) {
                    showRetryMergeBtn();
                }
            }
        }));
    }
    private void getDataSize() {
        h.removeMessages(MSG_GET_DATA_FRAME_SIZE);
        h.sendEmptyMessageDelayed(MSG_GET_DATA_FRAME_SIZE, 500);
    }

    private void setDataBump() {
        h.sendEmptyMessageDelayed(MSG_DATA_BUMP, (1+dataSize) * 1000 );
    }

    /////////////////////////// 合并数据 /////////////////////////////////////////
    /////////////////////////// magic-merge ////////////////////////////////////
    private void mergeData() {
        collectDataStatus = 100;
        funType = 1;
        showMergeUI();
        if(BuildConfig.DEBUG) superLogUtil.d("获取无线数据...");
        recvCount = 0;
        clearMsg();
        h.sendEmptyMessage(MSG_GET_DATA_FRAME_SIZE);
    }

    private void showMergeUI() {
        binding.tvTitle.setText(funType == 1 ? "数据合成" : "数据展示");
        binding.llLowPowerTip.setVisibility(View.GONE);
        binding.flStep.setVisibility(View.GONE);
        binding.llBtns.setVisibility(View.GONE);

        binding.tv23.setText(String.format("%.2f", oneDrillHoleInfo.collectCount * oneDrillHoleInfo.drillPipeLength / 100f));
        binding.tv24.setText(String.valueOf(oneDrillHoleInfo.collectCount));
        binding.tvCountTime.setStartTime(System.currentTimeMillis()-oneDrillHoleInfo.countTimeTotal*1000);

        binding.svCharts.setVisibility(View.VISIBLE);
        initChart(binding.chart1);
        initChart(binding.chart2);

        LimitLine ll1 = new LimitLine(0, "");
        ll1.setTextColor(Color.BLUE);
        ll1.setLineWidth(1f);
        ll1.setEnabled(true);
        ll1.setLineColor(Color.BLUE);
        ll1.enableDashedLine(5f, 10f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
        ll1.setTextSize(10f);
        binding.chart1.getAxisLeft().addLimitLine(ll1);

        ll1 = new LimitLine(0, "");
        ll1.setTextColor(Color.BLUE);
        ll1.setLineWidth(1f);
        ll1.setEnabled(true);
        ll1.setLineColor(Color.BLUE);
        ll1.enableDashedLine(5f, 10f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);//标签位置
        ll1.setTextSize(10f);
        binding.chart2.getAxisLeft().addLimitLine(ll1);

    }

    private Integer getHighLightIndex(LineChart chart) {
        Highlight[] highlighted = chart.getHighlighted();
        if( highlighted == null || highlighted.length == 0) return null;
        return (int)highlighted[0].getX();
    }

    private void initChart(LineChart chart) {

        chart.setBackgroundColor(Color.WHITE);

        chart.getDescription().setEnabled(false);

        chart.setTouchEnabled(false);


        chart.setDrawGridBackground(false);


        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);


        mv.setChartView(chart);
        chart.setMarker(mv);


        chart.setScaleEnabled(false);

        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        chart.getLegend().setEnabled(false);

        XAxis xAxis;
        {
            xAxis = chart.getXAxis();

            xAxis.setDrawGridLines(false);

        }

        YAxis yAxis;
        {
            yAxis = chart.getAxisLeft();

            chart.getAxisRight().setEnabled(false);

            yAxis.enableGridDashedLine(10f, 10f, 0f);

        }

        chart.setRenderer(new ZeusRender(chart, chart.getAnimator(), chart.getViewPortHandler()));

        binding.tvPrev.setOnClickListener( v-> {
            showPrevPoint();
        });
        binding.tvNext.setOnClickListener(v-> {
            showNextPoint();
        });
    }

    private void showPrevPoint() {
        FeedbackUtil.getInstance().doFeedback();
        curIdx--;
        doShowLightPoint(binding.chart1);
        doShowLightPoint(binding.chart2);
    }

    private void showNextPoint() {
        FeedbackUtil.getInstance().doFeedback();
        curIdx++;
        doShowLightPoint(binding.chart1);
        doShowLightPoint(binding.chart2);
    }

    private void doShowLightPoint(LineChart chart) {
        List<ILineDataSet> ds = chart.getLineData().getDataSets();
        ILineDataSet iLineDataSet = ds.get(1);

        binding.tvNext.setEnabled(curIdx < iLineDataSet.getEntryCount()-1);
        binding.tvPrev.setEnabled(curIdx > 0);

        if (curIdx >= iLineDataSet.getEntryCount()) {
            curIdx = iLineDataSet.getEntryCount() - 1;
            return;
        } else if(curIdx < 0) {
            curIdx = 0;
            return;
        }
        Entry entryForIndex = iLineDataSet.getEntryForIndex(curIdx);


        LinkedList<Entry> list = new LinkedList<>();
        list.add(entryForIndex);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(ds.get(0)); // add the data sets
        dataSets.add(ds.get(1)); // add the data sets
        dataSets.add(newLightSet(list));

        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);
        chart.invalidate();

        try {
            binding.chart1.highlightValue(entryForIndex.getX(), 1);
            binding.chart2.highlightValue(entryForIndex.getX(), 1);

            CollectTimeInfo info = timeList.get(curIdx);
            if (info == null) return;
            // 横滚角
            binding.tv11.setText(String.format("%.2f", info.rollAngle / 100f));
            // 俯仰角
            binding.tv12.setText(String.format("%.2f", info.omega / 100f));
            // 倾角值
            binding.tv13.setText(String.format("%.2f", info.slantAngle / 100f));
            // 方位角
            binding.tv14.setText(String.format("%.2f", info.directionAngle / 100f));
            binding.tv23.setText(String.format("%.2f", info.countId * oneDrillHoleInfo.drillPipeLength / 100f));
            binding.tv24.setText(String.valueOf(info.countId));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void loadCollectData() {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<CollectTimeInfo>>() {
            @Override
            public List<CollectTimeInfo> doInBackground() throws Throwable {
                //Thread.sleep(5000);
                return TrackerDBManager.getTimeList(oneDrillHoleInfo.id);
            }

            @Override
            public void onSuccess(List<CollectTimeInfo> result) {
                timeList = result;
                if (funType == 2 ) {
                    showChartWithData(result);
                } else {
                    mergeData();
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                ToastUtils.showLong("数据加载失败，请重试!");
            }
        });
    }

    private float fix2(float n) {
        int round = Math.round(n * 100);
        return round/100f;
    }

    private List<DataUtils.RealPoint> realPoints;
    private DataUtils dataUtils = new DataUtils();
    private void showChartWithData(List<CollectTimeInfo> result) {
        if (result == null || result.isEmpty()) return;
        int size = result.size();
        for (int i = 0; i < size; i++) {
            result.get(i).countId = i+1;
        }

        List<DataUtils.RealPoint> realPoints = dataUtils.calculatePoints(result, oneDrillHoleInfo.designAngle, oneDrillHoleInfo.designDirection, oneDrillHoleInfo.drillPipeLength);

        LinkedList<Entry> v1 = new LinkedList<Entry>();
        LinkedList<Entry> v2 = new LinkedList<Entry>();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        int count = realPoints.size();
        if (count <= 0) return;
        DataUtils.RealPoint first = realPoints.get(0);
        float v1max = first.high;
        float v1min = first.high;
        float v2max = first.dis;
        float v2min = first.dis;
        float xmax = first.space;
        float xmin = first.space;

        DataUtils.RealPoint last = realPoints.get(count - 1);
        boolean isSeq = last.space >= first.space;
        for (int i = 0; i < count; i++) {
            DataUtils.RealPoint info = realPoints.get(i);
            v1.add(new Entry((float) info.space, (float) info.high, result.get(i)));
            v2.add(new Entry((float) info.space, (float) info.dis, result.get(i)));

            sb1.append(info.space).append(",").append(info.high).append("->");
            sb2.append(info.space).append(",").append(info.dis).append("->");

            if (xmin > info.space) {
                xmin = info.space;
            }
            if (xmax < info.space) {
                xmax = info.space;
            }

            if (v1min > info.high) {
                v1min = info.high;
            }
            if (v1max < info.high) {
                v1max = info.high;
            }


            if (v2min > info.dis) {
                v2min = info.dis;
            }
            if (v2max < info.dis) {
                v2max = info.dis;
            }

        }


        if (count > 1) {
            float diff = 1f*Math.abs(realPoints.get(0).space - realPoints.get(1).space);
            xmin -= diff;
            xmax += diff;

            diff =  0.5f*Math.abs(realPoints.get(0).high - realPoints.get(1).high);
            v1min -= diff;
            v1max += diff;

            diff =  0.5f*Math.abs(realPoints.get(0).dis - realPoints.get(1).dis);
            v2min -= diff;
            v2max += diff;
        }
        binding.chart1.getXAxis().setAxisMinimum((float) xmin);
        binding.chart1.getXAxis().setAxisMaximum((float) xmax);

        float max = Math.max(Math.abs(v1max), Math.abs(v1min));
        binding.chart1.getAxisLeft().setAxisMinimum(-max);
        binding.chart1.getAxisLeft().setAxisMaximum(max);

        binding.chart2.getXAxis().setAxisMinimum((float) xmin);
        binding.chart2.getXAxis().setAxisMaximum((float) xmax);

        max = Math.max(Math.abs(v2max), Math.abs(v2min));
        binding.chart2.getAxisLeft().setAxisMinimum(-max);
        binding.chart2.getAxisLeft().setAxisMaximum(max);

        binding.chart2.getXAxis().setGranularity(0.01f);

        doShowChat(binding.chart1, v1);
        doShowChat(binding.chart2, v2);

        this.realPoints = realPoints;

        binding.chart1.invalidate();
        binding.chart2.invalidate();

        binding.llPrevNext.setVisibility(View.VISIBLE);
    }


    private LineDataSet newLightSet(LinkedList<Entry> lightValues) {
        LineDataSet set1 = new LineDataSet(lightValues, "");
        set1.setMode(LineDataSet.Mode.LINEAR);

        set1.setDrawValues(false);
        set1.setDrawIcons(false);
        set1.setColor(Color.RED);
        set1.setCircleHoleColor(Color.RED);
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(1f);

        set1.setCircleRadius(2f);
        set1.setValueTextSize(9f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setHighLightColor(Color.RED);
        set1.setDrawFilled(false);
        return set1;
    }

    private LineDataSet newOriginSet(LinkedList<Entry> lightValues) {
        LineDataSet set1 = new LineDataSet(lightValues, "");
        set1.setMode(LineDataSet.Mode.LINEAR);

        set1.setDrawValues(false);
        set1.setDrawIcons(false);
        set1.setColor(Color.BLACK);
        set1.setCircleHoleColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);

        set1.setCircleRadius(2f);
        set1.setValueTextSize(9f);
        set1.disableDashedLine();
        set1.setDrawFilled(false);
        return set1;
    }

    private void doShowChat(LineChart chart, LinkedList<Entry> values) {
        LineDataSet set1 = new LineDataSet(values, "l1");
        set1.setMode(LineDataSet.Mode.LINEAR);

        set1.setDrawValues(false);
        set1.setDrawIcons(false);
        set1.setColor(Color.parseColor("#0E65EE"));
        set1.setCircleHoleColor(Color.parseColor("#0E65EE"));
        set1.setCircleColor(Color.parseColor("#0E65EE"));
        set1.setColor(Color.BLUE);
        set1.setLineWidth(1f);

        set1.setCircleRadius(2f);
        /*set1.setValueTextSize(9f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setHighLightColor(Color.RED);*/
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        LinkedList<Entry> list = new LinkedList<>();
        list.add(new Entry(0, 0));
        if (values.size() > 0) {
            Entry first = values.get(0);
            Entry last = values.getLast();
            if (Math.abs(first.getX()) < Math.abs(last.getX())) {
                list.add(first);
            } else {
                list.getLast();
            }
        }
        dataSets.add(newOriginSet(list));

        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);

    }
}