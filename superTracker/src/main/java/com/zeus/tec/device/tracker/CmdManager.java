package com.zeus.tec.device.tracker;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
// FB 05 01 4F 4B A0
// FB 05 01 45 52 9D
public class CmdManager {
    public static byte[] SUCCESS_FRAME = new byte[]{(byte) 0xFB,0x05,0x01,0x4F,0x4B, (byte) 0xA0};
    public static byte[] FAILED_FRAME = new byte[]{(byte) 0xFB,0x05,0x01,0x45,0x52, (byte) 0x9D};
    public static final byte FRAME_HEADER = (byte)0xfb;
    // 打开电源开关
    public static byte[] turnOnPowerFrame() {
        return formatFrame((byte) 0x01, new byte[]{0x01});
    }
    // 关闭电源开关
    public static byte[] turnOffPowerFrame() {
        return formatFrame((byte) 0x01, new byte[]{0x00});
    }
    // 启动上传探头中存储的数据
    public static byte[] startUploadDataFrame() {
        return formatFrame((byte) 0x02, new byte[]{0x01});
    }
    // 设置水平安装
    public static byte[] setHorizontalInstallFrame() {
        return formatFrame((byte) 0x04, new byte[]{0x00});
    }
    // 设置垂直安装
    public static byte[] setVerticalInstallFrame() {
        return formatFrame((byte) 0x04, new byte[]{0x01});
    }
    // 设置水平垂直自动调整安装
    public static byte[] setAutoInstallFrame() {
        return formatFrame((byte) 0x04, new byte[]{0x02});
    }
    // 进入有线模式
    public static byte[] enterWireModeFrame() {
        return formatFrame((byte) 0x06, new byte[]{0x01});
    }
    // 退出有线模式
    public static byte[] exitWireModeFrame() {
        return formatFrame((byte) 0x06, new byte[]{0x00});
    }
    //开始校准罗盘
    public static byte[] startAdjustCompassFrame() {
        return formatFrame((byte) 0x07, new byte[]{0x01});
    }
    //停止校准罗盘
    public static byte[] stopAdjustCompassFrame() {
        return formatFrame((byte) 0x07, new byte[]{0x00});
    }
    //保存校准结果
    public static byte[] saveAdjustCompassFrame() {
        return formatFrame((byte) 0x07, new byte[]{0x02});
    }
    // 有线模式开始采集数据
    public static byte[] startCollectFrame() {
        return formatFrame((byte) 0x08, new byte[]{0x01});
    }
    // 有线模式停止采集数据
    public static byte[] stopCollectFrame() {
        return formatFrame((byte) 0x08, new byte[]{0x00});
    }
    //手动擦除探头中的数据
    public static byte[] clearDataFrame() {
        return formatFrame((byte) 0x09, new byte[]{0x01});
    }
    //获取探头中存储的数据组数
    public static byte[] getDataFrameSize() {
        return formatFrame((byte) 0x0a, new byte[]{0x01});
    }
    //返回：FB 08 0A 01 00 00 00 00 CRC
    //红色部分为数组数量，u32类型
    public static long getDateLength(byte[] frame) {
        if (frame == null || frame.length < 9) return -1;
        if (frame[0] != FRAME_HEADER) return -1;
        if (frame[1] != 0x08) return -1;
        if (frame[2] != 0x0A) return -1;
        if (frame[3] != 0x01) return -1;
        if (frame[8] != check(Arrays.copyOfRange(frame, 1, 8))) return -1;
        return bytes2long(Arrays.copyOfRange(frame, 4, 8));
    }
    private static byte check(byte[] bytes) {
        byte check = 0;
        for (int i=0; i<bytes.length; i++) {
            check += bytes[i];
        }
        return check;
    }
    private static long bytes2long(byte[] bytes) {
        long value = 0l;
        for (byte b : bytes) {
            value = (value << 8) + (b & 255);
        }
        return value;
    }
    // 开始无线采集
    public static byte[] startWirelessCollect() {
        return formatFrame((byte) 0x0b, new byte[]{0x01});
    }
    // 设置探头时间
    public static byte[] setTimeFrame(long time) {
        byte[] data = new byte[6];
        Calendar cl = Calendar.getInstance();
        cl.setTime(new Date(time));
        data[0] = (byte)(cl.get(Calendar.YEAR) % 2000);
        data[1] = (byte)(cl.get(Calendar.MONTH) + 1);
        data[2] = (byte)(cl.get(Calendar.DATE));
        data[3] = (byte)(cl.get(Calendar.HOUR_OF_DAY));
        data[4] = (byte)(cl.get(Calendar.MINUTE));
        data[5] = (byte)(cl.get(Calendar.SECOND));
        return formatFrame((byte) 0x0c, data);
    }
    // 设置角速度阈值 u16
    public static byte[] setAngleSpeedThresholdFrame(int speed) {
        byte[] data = new byte[2];
        data[0] = (byte) ((speed>>8)&0xff);
        data[1] = (byte) (speed&0xff);
        return formatFrame((byte) 0x0d, data);
    }

    public static byte[] formatFrame(byte cmd, byte[] data) {
        byte[] frame = new byte[data.length+4];
        byte check = 0;
        int start = 0;
        frame[start] = FRAME_HEADER;
        start++;
        frame[start] = (byte)(3 + data.length);
        check += frame[start];
        start++;
        frame[start] = cmd;
        check += frame[start];
        start++;

        for (int i=0; i<data.length; i++) {
            frame[start] = data[i];
            check += frame[start];
            start++;
        }
        frame[start] = (byte)(check&0xff);
        start++;
        return frame;
    }
}
