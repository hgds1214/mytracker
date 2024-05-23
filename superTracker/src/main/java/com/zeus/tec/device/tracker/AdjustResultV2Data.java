package com.zeus.tec.device.tracker;

import java.util.Arrays;

// FB 10 00 89 00 00 78 00 01 13 00 00 40 00 64 74 5B
public class AdjustResultV2Data {
    public int flag;
    public int x;
    public int y;
    public int z;

    public static AdjustResultV2Data fromFrame(byte[] frame) {

        // 一帧数据总字节数为19
        if (frame == null || frame.length != 9) return null;

        if (frame[0] != CmdManager.FRAME_HEADER) return null;
        if (frame[1] != 0x08) return null;
        if (frame[2] != 0) return null;
        if ((int)(frame[3]&0xff)  != 0x89) return null;
        if (frame[frame.length-1] != check(Arrays.copyOfRange(frame, 1, frame.length-1))) return null;

        AdjustResultV2Data result = new AdjustResultV2Data();
        int start = 4;
        result.flag = frame[start]&0xff;
        result.x = frame[start]&0xff;
        start += 1;
        result.y = frame[start]&0xff;
        start += 1;
        result.z = frame[start]&0xff;
        start += 1;
        return result;
    }

    private static byte check(byte[] bytes) {
        byte check = 0;
        for (int i=0; i<bytes.length; i++) {
            check += bytes[i];
        }
        return check;
    }

    private static float bytes2float(byte[] bytes) {
        long r = (bytes[0]&0xf) * 10000;
        r += ((bytes[1]>>4)&0xf) * 1000;
        r += (bytes[1]&0xf)*100;

        r += ((bytes[2]>>4)&0xf) * 10;
        r += (bytes[2]&0xf);

        if (((bytes[0]>>4)&0xf) != 0) {
            return -(r/100.0f);
        } else {
            return (r/100.0f);
        }
    }
}
