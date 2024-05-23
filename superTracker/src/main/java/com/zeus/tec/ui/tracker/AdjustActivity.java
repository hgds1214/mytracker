package com.zeus.tec.ui.tracker;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.databinding.ActivityAdjustBinding;
import com.zeus.tec.device.tracker.AdjustResultData;
import com.zeus.tec.device.tracker.AdjustResultV2Data;
import com.zeus.tec.device.tracker.CmdManager;
import com.zeus.tec.device.usbserial.ParserCenter;
import com.zeus.tec.device.usbserial.USBSerialManager;
import com.zeus.tec.event.AdjustNumberEvent;
import com.zeus.tec.event.AdjustResultEvent;
import com.zeus.tec.ui.base.BaseActivity;
import com.zeus.tec.ui.tracker.config.SystemConfig;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class AdjustActivity extends BaseActivity implements USBSerialManager.USBStateChangeListener, Handler.Callback {
    private static final String KEY_ENTER_WIRE_MODE = "KEY_DETECT_HEALTH_01";
    private static final String KEY_EXIT_WIRE_MODE = "KEY_EXIT_WIRE_MODE";
    private static final String KEY_START_ADJUST = "KEY_START_ADJUST";
    private static final String KEY_STOP_ADJUST = "KEY_STOP_ADJUST";
    private static final String KEY_SAVE_ADJUST = "KEY_SAVE_ADJUST";

    private ActivityAdjustBinding binding;
    private SuperLogUtil superLogUtil;
    private Handler h = new Handler(Looper.getMainLooper(), this);


    public static final int MSG_ENTER_WIRE_MODE = 1;
    public static final int MSG_EXIT_WireMode = 2;
    public static final int MSG_ADJUST_START = 3;
    public static final int MSG_ADJUST_STOP = 4;
    public static final int MSG_ADJUST_SAVE = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdjustBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        superLogUtil = new SuperLogUtil(this);
        EventBus.getDefault().register(this);

        binding.ivBack.setOnClickListener(v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
        });

        binding.tvLastTime.setText(SystemConfig.getLastAdjustTimeText());
        binding.tvLastAddress.setText(SystemConfig.getLastAdjustAddress());

        binding.tvOk.setOnClickListener(v -> onClickAdjust());
        binding.tvSave.setOnClickListener( v->onClickSave());
        binding.tvCancel.setOnClickListener( v->{
            FeedbackUtil.getInstance().doFeedback();
            finish();
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

    private void onClickSave() {
        FeedbackUtil.getInstance().doFeedback();
        if (binding.edtAdjustAddress.getText().toString().isEmpty()) {
            ToastUtils.showLong("请输入校准地点");
            return;
        }
        h.sendEmptyMessage(MSG_ADJUST_SAVE);
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
        FeedbackUtil.getInstance().doFeedback();
        if ( state == 0) {
            step02();
            return;
        }
    }


    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    private void step02() {
        state = 1;
        time = System.currentTimeMillis();
        binding.tvCurrentTime.setText(sdf.format(time));
        binding.llAdjustInfo.setVisibility(View.VISIBLE);
        binding.tvOk.setVisibility(View.GONE);
        h.sendEmptyMessage(MSG_ADJUST_START);
    }

    @Override
    public void onDeviceAttached() {
        if (BuildConfig.DEBUG) superLogUtil.d( "检测到USB设备");

        USBSerialManager.getInstance().start(this);
    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.tvStatus.setText("设备已连接");
                clearMsg();
                h.sendEmptyMessageDelayed(MSG_ENTER_WIRE_MODE, 1000);
            }
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(this::resetUI);
    }

    private void resetUI() {
        clearMsg();
        state = 0;
        binding.tvStatus.setText("等待设备连接");
        binding.llAdjustInfo.setVisibility(View.GONE);
        binding.tvOk.setEnabled(false);
        binding.tvOk.setText("开始罗盘校准");
        binding.tvOk.setVisibility(View.VISIBLE);
    }

    private void showResultUI(AdjustResultData resultData) {
        binding.tvResult.setVisibility(View.VISIBLE);
        binding.tvResultDesc.setVisibility(View.VISIBLE);
        binding.llStep.setVisibility(View.GONE);
        binding.tvStatus.setText("校准结束");
        StringBuilder sb = new StringBuilder();
        sb.append("校准残差系数:").append(resultData.a).append("\n");
        sb.append("均匀程度:").append(resultData.x).append("%\n");
        sb.append("倾斜角度分布范围:").append(resultData.y).append("\n");
        sb.append("俯仰角和横滚角的最大角单边幅值:").append(resultData.z);
        binding.tvResult.setText(sb.toString());

        sb = new StringBuilder();
        sb.append("说明：\n");
        sb.append("1.采样点的校准残差系数，<1为正常，该值越小，表示该次校准越靠前；\n");
        sb.append("2.采样点的分布在各个方位的均匀程度，该得分大概<6%,此值越小，表示采样点分布越均匀；\n");
        sb.append("3.倾斜角度分布范围，得分应0~1之间，此值越小，表示采样点在空间覆盖越广泛；\n");
        sb.append("4.俯仰角度何横滚角度的最大角度单边幅值，该得分应>45°，此值越大，表示该次校准采样点在空间分布越充分；\n");
        sb.append("注意：以上打分值仅供参考，满足以上分值要求，表示该次校准采样条件优良，并不一定得到精确的方位精度。");
        binding.tvResultDesc.setText(sb.toString());

        binding.tvOk.setVisibility(View.GONE);
        binding.tvCancel.setVisibility(View.VISIBLE);
        binding.tvSave.setVisibility(View.VISIBLE);
    }

    private void saveSuccess() {
        SystemConfig.setLastAdjustTime(time);
        SystemConfig.setLastAdjustAddress(binding.edtAdjustAddress.getText().toString());
        ToastUtils.showLong("罗盘校准成功");
        finish();
    }
    private void showResultUI(AdjustResultV2Data resultData) {
        binding.tvResult.setVisibility(View.VISIBLE);
        binding.tvResultDesc.setVisibility(View.GONE);
        binding.llStep.setVisibility(View.GONE);
        binding.tvStatus.setText("校准结束");
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) binding.tvStatus.getLayoutParams();
        lp.topMargin = 0;
        binding.tvStatus.setLayoutParams(lp);

        StringBuilder sb = new StringBuilder();
        sb.append("X:").append(resultData.x).append("\n");
        sb.append("Y:").append(resultData.y).append("\n");
        sb.append("Z:").append(resultData.z).append("\n");

        binding.tvResult.setText(sb.toString());

        binding.tvOk.setVisibility(View.GONE);
        binding.tvCancel.setVisibility(View.VISIBLE);
        binding.tvSave.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdjustNumberEvent(AdjustNumberEvent e) {
        if (state == 0) return;
        binding.tvStep.setText(String.valueOf(e.number));

        if (e.number == 24) {
            clearMsg();
            Message msg = Message.obtain();
            msg.what = MSG_ADJUST_STOP;
            msg.arg1 = 3;
            h.sendMessage(msg);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdjustResultEvent(AdjustResultEvent e) {
        if (state == 0) return;
        if (e == null) return;
        if (e.resultData != null) {
            showResultUI(e.resultData);
        } else if (e.arv2Data != null) {
            showResultUI(e.arv2Data);
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_ENTER_WIRE_MODE:
                doEnterWireMode();
                break;
            case MSG_EXIT_WireMode:
                doExitWireCollectMode();
                break;
            case MSG_ADJUST_START:
                doStartAdjust();
                break;
            case MSG_ADJUST_STOP:
                doStopAdjust(msg.arg1);
                break;
            case MSG_ADJUST_SAVE:
                doSave();
                break;
        }
        return false;
    }

    private void doSave() {
        if (BuildConfig.DEBUG) superLogUtil.d("04: 发送 保存校准命令");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_SAVE_ADJUST, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.saveAdjustCompassFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_SAVE_ADJUST) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("04: 保存校准命令 执行成功");

                saveSuccess();
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_SAVE_ADJUST) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("04: 保存校准命令 执行失败: " + error);
                if (USBSerialManager.getInstance().isConnected()) {
                    ToastUtils.showLong("设备通信失败，请检查连接线");
                } else {
                    ToastUtils.showLong("设备通信失败，请检查");
                }
                clearMsg();
            }
        }));
    }

    private void doStopAdjust(final int retry) {
        if (BuildConfig.DEBUG) superLogUtil.d("03: 发送 停止校准命令");
        binding.tvOk.setEnabled(false);

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_STOP_ADJUST, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.stopAdjustCompassFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_STOP_ADJUST) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("03: 停止校准命令 执行成功");

            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_STOP_ADJUST) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("03: 停止校准命令 执行失败: " + error);
                if (USBSerialManager.getInstance().isConnected()) {
                    ToastUtils.showLong("设备通信失败，请检查连接线");
                } else {
                    ToastUtils.showLong("设备通信失败，请检查");
                }
                clearMsg();
                if (retry <= 0) {
                    state = 0;
                    binding.tvOk.setEnabled(true);
                    binding.tvOk.setVisibility(View.VISIBLE);
                } else {
                    Message msg = Message.obtain();
                    msg.what = MSG_ADJUST_STOP;
                    msg.arg1 = retry - 1;
                    h.sendMessageDelayed(msg, 2000);
                }
            }
        }));
    }



    private void doStartAdjust() {
        if (BuildConfig.DEBUG) superLogUtil.d("02: 发送 开始校准命令");
        binding.tvOk.setEnabled(false);

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_START_ADJUST, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.startAdjustCompassFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_START_ADJUST) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 开始校准命令 执行成功");

                binding.tvStatus.setText("罗盘校准中...");
                clearMsg();
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_START_ADJUST) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("02: 开始校准命令 执行失败: " + error);
                if (USBSerialManager.getInstance().isConnected()) {
                    ToastUtils.showLong("设备通信失败，请检查连接线");
                } else {
                    ToastUtils.showLong("设备通信失败，请检查");
                }
                clearMsg();
                binding.tvOk.setVisibility(View.VISIBLE);
            }
        }));
    }

    private void clearMsg() {
        h.removeCallbacksAndMessages(null);
    }

    private HashMap<String, String> map = new HashMap<>();
    private long countId = 1;
    private void doEnterWireMode() {
        if (BuildConfig.DEBUG) superLogUtil.d("01: 发送 进入有线模式命令");

        countId++;
        final String id = String.valueOf(countId);
        map.put(KEY_ENTER_WIRE_MODE, id);

        USBSerialManager serialManager = USBSerialManager.getInstance();
        serialManager.sendFrame(new ParserCenter.FrameWrapper(CmdManager.enterWireModeFrame(), new ParserCenter.SendCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if (map.get(KEY_ENTER_WIRE_MODE) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("01: 进入有线模式命令 执行成功");

                clearMsg();
                state = 0;
                binding.tvOk.setVisibility(View.VISIBLE);
                binding.tvOk.setText("开始罗盘校准");
                binding.tvOk.setEnabled(true);
                binding.tvStatus.setText("设备运行中");
            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_ENTER_WIRE_MODE) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("01: 进入有线模式命令 执行失败: " + error);
                if (USBSerialManager.getInstance().isConnected()) {
                    ToastUtils.showLong("设备通信失败，请检查连接线");
                } else {
                    ToastUtils.showLong("设备通信失败，请检查");
                }
                clearMsg();
                h.sendEmptyMessageDelayed(MSG_ENTER_WIRE_MODE, USBSerialManager.getInstance().isConnected()?5000:10000);
            }
        }));
    }

    private void doExitWireCollectMode() {
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

                ToastUtils.showLong("保存成功");

            }

            @Override
            public void onFail(String error) {
                if (map.get(KEY_EXIT_WIRE_MODE) != id) return;

                if (BuildConfig.DEBUG) superLogUtil.d("04: 退出有线模式命令 执行失败:"+error);

                ToastUtils.showLong("进入无线模式失败，请重试！");
            }
        }));
    }

}