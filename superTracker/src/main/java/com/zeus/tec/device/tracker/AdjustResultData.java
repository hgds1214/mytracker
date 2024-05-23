package com.zeus.tec.device.tracker;

import java.util.Arrays;

// FB 10 00 89 00 00 78 00 01 13 00 00 40 00 64 74 5B
public class AdjustResultData {
    public float a;
    public float x;
    public float y;
    public float z;

    public static AdjustResultData fromFrame(byte[] frame) {

        // 一帧数据总字节数为19
        if (frame == null || frame.length != 17) return null;

        if (frame[0] != CmdManager.FRAME_HEADER) return null;
        if (frame[1] != 0x10) return null;
        if (frame[2] != 0) return null;
        if (frame[3] != (byte)0x89) return null;
        if (frame[frame.length-1] != check(Arrays.copyOfRange(frame, 1, frame.length-1))) return null;

        AdjustResultData result = new AdjustResultData();
        int start = 4;
        result.a = bytes2float(Arrays.copyOfRange(frame, start, start+3));
        start += 3;
        result.x = bytes2float(Arrays.copyOfRange(frame, start, start+3));
        start += 3;
        result.y = bytes2float(Arrays.copyOfRange(frame, start, start+3));
        start += 3;
        result.z = bytes2float(Arrays.copyOfRange(frame, start, start+3));
        start += 3;
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
