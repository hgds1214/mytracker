package com.zeus.tec.device.usbserial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.hoho.android.usbserial.util.HexDump;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.device.tracker.AdjustData;
import com.zeus.tec.device.tracker.AdjustResultData;
import com.zeus.tec.device.tracker.AdjustResultV2Data;
import com.zeus.tec.device.tracker.CmdManager;
import com.zeus.tec.device.tracker.CompassData;
import com.zeus.tec.device.tracker.DataParser;
import com.zeus.tec.device.tracker.TrackerCollectData;
import com.zeus.tec.event.AdjustNumberEvent;
import com.zeus.tec.event.AdjustResultEvent;
import com.zeus.tec.event.TrackerCollectDataEvent;
import com.zeus.tec.event.TrackerDataSizeEvent;
import com.zeus.tec.event.WireCollectDataEvent;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class ParserCenter implements Handler.Callback, OnParseOneFrameCallback {
    public static final int CMD_TIMEOUT = 2*1000; // 2 s

    private static final int MSG_SEND_FRAME = 1;
    private static final int MSG_SEND_FRAME_TIMEOUT = 2;
    private static final int MSG_RECV_FRAME = 3;

    private Queue<FrameWrapper> sendQueue = new LinkedBlockingDeque<>();

    private DataParser dataParser = new DataParser();

    private HandlerThread handlerThread = new HandlerThread("data-parser-thread");
    private Handler h;

    public ParserCenter() {
        handlerThread.start();
//        h = new Handler(handlerThread.getLooper(), this);
        h = new Handler(Looper.getMainLooper(), this);
    }


    public void addData(byte[] data, OnParseOneFrameCallback onParseOneFrameCallback) {
        dataParser.addData(data, this);
    }

    public void setBufferSize(int maxSize) {
        SuperLogUtil.sd("setBufferSize: " + maxSize);
        dataParser.setBufferSize(Math.max(8196, maxSize));
    }

    private long lastSendTime = 0;
    public void sendFrame(FrameWrapper frameWrapper) {
        Message msg = Message.obtain();
        msg.what = MSG_SEND_FRAME;
        msg.obj = frameWrapper;
        h.sendMessage(msg);
//        long curStamp = SystemClock.elapsedRealtime();
//        long diff = curStamp - lastSendTime;
//        final long MIN_DIFF = (CMD_TIMEOUT*3/2);
//        if (diff > MIN_DIFF) {
//            h.sendMessage(msg);
//            lastSendTime = curStamp;
//            if (BuildConfig.DEBUG) {
//                SuperLogUtil.sd("立刻发送");
//            }
//        } else {
//            h.sendMessageDelayed(msg, MIN_DIFF -diff);
//            lastSendTime += MIN_DIFF;
//            if (BuildConfig.DEBUG) {
//                SuperLogUtil.sd("延迟发送");
//            }
//        }
    }
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_SEND_FRAME:
                doSendFrame((FrameWrapper) msg.obj);
                break;
            case MSG_SEND_FRAME_TIMEOUT:
                doSendFrameTimeout((FrameWrapper) msg.obj);
                break;
            case MSG_RECV_FRAME:
                doRecvFrame((FrameWrapper) msg.obj);
                break;
        }
        return false;
    }

    private void doSendFrame(FrameWrapper frameWrapper) {
        if (frameWrapper == null || frameWrapper.data == null) return;
        long t = SystemClock.elapsedRealtime();
        if (BuildConfig.DEBUG) {
            SuperLogUtil.sd( "发送命令：" + HexDump.dumpHexString(frameWrapper.data));
        }
        boolean ret = USBSerialManager.getInstance().send(frameWrapper.data);

        //if (BuildConfig.DEBUG) SuperLogUtil.sd("命令成功发送：" + ret);

        if (!ret) {
            frameWrapper.sendCallback.onFail("发送命令失败，isConnected:"+USBSerialManager.getInstance().isConnected());
            return;
        }

        if (frameWrapper.sendCallback == null ) return;

        frameWrapper.timestamp = t;
        sendQueue.offer(frameWrapper);
        Message msg = Message.obtain();
        msg.what = MSG_SEND_FRAME_TIMEOUT;
        msg.obj = frameWrapper;
        h.sendMessageDelayed(msg, CMD_TIMEOUT);
    }
    private void doSendFrameTimeout(FrameWrapper frameWrapper) {
        if (frameWrapper == null || frameWrapper.sendCallback == null) return;

        if (sendQueue.contains(frameWrapper)) {
            if (BuildConfig.DEBUG) {
                SuperLogUtil.sd("超时炸弹，没有收到返回数据");
            }
            sendQueue.remove(frameWrapper);
            frameWrapper.sendCallback.onFail("没有接收到返回命令");
        }
    }

    @Override
    public void onParseOneFrame(byte[] frame) {
        /*if (BuildConfig.DEBUG) SuperLogUtil.sd("onParseOneFrame: " + frame.length);*/

        Message msg = Message.obtain();
        msg.what = MSG_RECV_FRAME;
        msg.obj = FrameWrapper.newRecvFrame(frame);
        h.sendMessage(msg);
    }

    private void doRecvFrame(FrameWrapper frameWrapper) {
        byte[] frame = frameWrapper.data;

//        if (BuildConfig.DEBUG) SuperLogUtil.sd("doRecvFrame: " + frame.length);

        if (frame == null) return;

        long timeRecv = frameWrapper.timestamp;
        FrameWrapper peek = null;
        while (!sendQueue.isEmpty()) {
            peek = sendQueue.peek();
            if (timeRecv - peek.timestamp <= CMD_TIMEOUT) {
                break;
            }
            if (peek.sendCallback != null) {
                if(BuildConfig.DEBUG) SuperLogUtil.sd("收到数据判断出超时命令");
                peek.sendCallback.onFail("多余超时命令");
            }
            sendQueue.poll();
        }
        if (Arrays.equals(frame, CmdManager.SUCCESS_FRAME)) {

            if(BuildConfig.DEBUG) SuperLogUtil.sd("解析到 成功操作 数据");

            if (peek != null) {
                sendQueue.poll();
                if (peek.sendCallback != null) peek.sendCallback.onSuccess(frame);
            }
            return;
        } else if (Arrays.equals(frame, CmdManager.FAILED_FRAME)) {
            if(BuildConfig.DEBUG){
                ToastUtils.showLong("解析到 操作失败 数据");
                SuperLogUtil.sd("解析到 操作失败 数据");
            }
            if (peek != null) {
                sendQueue.poll();
                if (peek.sendCallback != null) peek.sendCallback.onFail("命令执行错误");
            }
            return;
        }

        // 查看是否为有线模式上传数据格式
        CompassData compassData = CompassData.fromFrame(frame);
        if (compassData != null) {
            SuperLogUtil.sd("有线采集数据："+HexDump.dumpHexString(frame));
            EventBus.getDefault().post(new WireCollectDataEvent(compassData));
            return;
        }

        TrackerCollectData trackerCollectData = TrackerCollectData.fromFrame(frame);
        if (trackerCollectData != null) {
            SuperLogUtil.sd("无线数据帧："+HexDump.dumpHexString(frame));
            EventBus.getDefault().post(new TrackerCollectDataEvent(trackerCollectData));
            return;
        }

        long dataSize = CmdManager.getDateLength(frame);
        if (dataSize >= 0) {
            //SuperLogUtil.sd("解析到 探头数据组数");
            EventBus.getDefault().post(new TrackerDataSizeEvent(dataSize));
            return;
        }

        AdjustData adjustData = AdjustData.fromFrame(frame);
        if (adjustData != null) {
            SuperLogUtil.sd("校准数据帧："+HexDump.dumpHexString(frame));
            EventBus.getDefault().post(new AdjustNumberEvent(adjustData.number));
            return;
        }

        AdjustResultV2Data arv = AdjustResultV2Data.fromFrame(frame);
        if (arv != null) {
            SuperLogUtil.sd("校准结果数据帧："+HexDump.dumpHexString(frame));
            EventBus.getDefault().post(new AdjustResultEvent(arv));
            return;
        }

        AdjustResultData resultData = AdjustResultData.fromFrame(frame);
        if (resultData != null) {
            SuperLogUtil.sd("校准结果数据帧："+HexDump.dumpHexString(frame));
            EventBus.getDefault().post(new AdjustResultEvent(resultData));
            return;
        }

        if(BuildConfig.DEBUG){
            SuperLogUtil.sd("解析到 其他 数据");
            SuperLogUtil.sd(HexDump.dumpHexString(frame));
        }
    }


    public static class FrameWrapper {
        public boolean isSend = true;
        public long timestamp;
        public byte[] data;
        public SendCallback sendCallback;

        public FrameWrapper(byte[] data, SendCallback sendCallback) {
            this.data = data;
            this.sendCallback = sendCallback;
        }

        public FrameWrapper(byte[] data) {
            this.isSend = false;
            this.timestamp = SystemClock.elapsedRealtime();
            this.data = data;
        }
        public static FrameWrapper newSendFrame(byte[] data, SendCallback sendCallback) {
            return new FrameWrapper(data, sendCallback);
        }

        public static FrameWrapper newRecvFrame(byte[] data) {
            return new FrameWrapper(data);
        }
    }

    public interface SendCallback {
        void onSuccess(byte[] data);
        void onFail(String error);
    }
}
