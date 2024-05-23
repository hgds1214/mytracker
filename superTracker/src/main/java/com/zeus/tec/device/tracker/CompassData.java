package com.zeus.tec.device.tracker;

import com.zeus.tec.BuildConfig;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import java.util.Arrays;
import java.util.Calendar;

public class CompassData {
    //方位 角(2B)MSB,
    //u16类型
    public int directionAngle;
    //俯仰角(2B)MSB,
    //short类型
    public short omega;
    // 横滚角(2B)MSB,
    //short类型
    public short rollAngle;
    //倾斜角(2B)MSB,
    //short类型
    public short slantAngle;
    //电池电压 2b,2位小数
    public float voltage;
    // 年 月 日 时 分 秒
    public long time;

    public static CompassData fromFrame(byte[] frame) {
        // 一帧数据总字节数为19
        if (frame == null || frame.length != 0x14) return null;

        if (frame[0] != CmdManager.FRAME_HEADER) return null;
        if (frame[1] != 0x13) return null;
        if (frame[2] != 0x1) return null;
        if (frame[frame.length-1] != check(Arrays.copyOfRange(frame, 1, frame.length-1))) return null;

        CompassData result = new CompassData();
        int start = 3;
        result.directionAngle = bytes2int(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        result.omega = bytes2short(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        result.rollAngle = bytes2short(Arrays.copyOfRange(frame, start, start+2));
        start += 2;
        result.slantAngle = bytes2short(Arrays.copyOfRange(frame, start, start+2));
        start += 2;

        float vol = (int)frame[start] + (((int)frame[start+1]) / 100f);
        start += 2;
        result.voltage = vol;

        if( BuildConfig.DEBUG) {
            SuperLogUtil.sd("compass:" + (frame[start] + 2000) + "-" + (frame[start + 1] + 1) + "-" + frame[start + 2] +
                    " " + frame[start + 3] + ":" + frame[start + 4] + ":" + frame[start + 5]);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, frame[start]+2000);
        start++;
        calendar.set(Calendar.MONTH, frame[start]+1);
        start++;
        calendar.set(Calendar.DAY_OF_MONTH, frame[start]);
        start++;
        calendar.set(Calendar.HOUR_OF_DAY, frame[start]);
        start++;
        calendar.set(Calendar.MINUTE, frame[start]);
        start++;
        calendar.set(Calendar.SECOND, frame[start]);
        start++;
        result.time = calendar.getTimeInMillis();
        return result;
    }

    private static byte check(byte[] bytes) {
        byte check = 0;
        for (int i=0; i<bytes.length; i++) {
            check += bytes[i];
        }
        return check;
    }

    private static int bytes2int(byte[] bytes) {
        int result = 0;
        for (int i=0; i<bytes.length; i++) {
            result = (result << 8);
            result += (bytes[i]&0xff);
        }
        return result;
    }
    private static short bytes2short(byte[] bytes) {
        short result = 0;
        for (int i=0; i<bytes.length; i++) {
            result = (short) (result << 8);
            result += (bytes[i]&0xff);
        }
        return result;
    }


}
