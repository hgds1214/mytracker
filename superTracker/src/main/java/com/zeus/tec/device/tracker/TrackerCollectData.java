package com.zeus.tec.device.tracker;

import com.blankj.utilcode.util.ConvertUtils;
import com.zeus.tec.ui.tracker.util.TimeUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TrackerCollectData {
    //存储点的序号(4B)MSB
    //u32类型
    public long serialId;
    //采集点的时间(4B)MSB
    //u32类型
    public long collectTime;
    // 横滚角(2B)MSB,
    //short类型
    public short rollAngle;
    //俯仰角(2B)MSB,
    //short类型
    public short omega;
    //方位 角(2B)MSB,
    //u16类型
    public int directionAngle;
    //倾斜角(2B)MSB,
    //short类型
    public short slantAngle;

    public static TrackerCollectData fromFrame(byte[] frame) {
        // 一帧数据总字节数为19
        if (frame == null || frame.length != 0x13) return null;
        if (frame[0] != CmdManager.FRAME_HEADER) return null;
        if (frame[1] != 0x12) return null;
        if (frame[frame.length-1] != check(Arrays.copyOfRange(frame, 1, frame.length-1))) return null;

        TrackerCollectData result = new TrackerCollectData();
        int start = 2;
        result.serialId = bytes2long(Arrays.copyOfRange(frame, start, start+4));
        start += 4;

        long collectTime = bytes2long(Arrays.copyOfRange(frame, start, start+4));
        start += 4;

        result.collectTime = collectTime;//TimeUtil.getDateTime(collectTime);

        result.rollAngle = bytes2short(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        result.omega = bytes2short(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        result.directionAngle = bytes2int(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        result.slantAngle = bytes2short(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        return result;
    }

    private static byte check(byte[] bytes) {
        byte check = 0;
        for (int i=0; i<bytes.length; i++) {
            check += bytes[i]& 0xff;
        }
        return check;
    }

    private static int bytes2int(byte[] bytes) {
        int result = 0;
        for (int i=0; i<bytes.length; i++) {
            result = (result << 8);
            result += bytes[i]& 0xff;
        }
        return result;
    }
    private static short bytes2short(byte[] bytes) {
        short result = 0;
        for (int i=0; i<bytes.length; i++) {
            result = (short) (result << 8);
            result += bytes[i] & 0xff;
        }
        return result;
    }
    private static long bytes2long(byte[] bytes) {
        long value = 0l;
        for (byte b : bytes) {
            value = (value << 8) + (b & 255);
        }
        return value;
    }
}
