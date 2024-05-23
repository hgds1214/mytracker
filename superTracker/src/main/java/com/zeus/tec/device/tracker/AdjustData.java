package com.zeus.tec.device.tracker;

import java.util.Arrays;

public class AdjustData {
    public int number;

    public static AdjustData fromFrame(byte[] frame) {
        // 一帧数据总字节数为19
        if (frame == null || frame.length != 6) return null;

        if (frame[0] != CmdManager.FRAME_HEADER) return null;
        if (frame[1] != 5) return null;
        if (frame[2] != 0) return null;
        if ((int)(frame[3]&0xff) != 0x88) return null;
        if (frame[frame.length-1] != check(Arrays.copyOfRange(frame, 1, frame.length-1))) return null;

        AdjustData result = new AdjustData();
        result.number = frame[4]&0xff;
        return result;
    }

    private static byte check(byte[] bytes) {
        byte check = 0;
        for (int i=0; i<bytes.length; i++) {
            check += bytes[i];
        }
        return check;
    }
}
