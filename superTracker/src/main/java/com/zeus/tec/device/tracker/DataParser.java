package com.zeus.tec.device.tracker;

import com.zeus.tec.device.usbserial.IDataParser;
import com.zeus.tec.device.usbserial.OnParseOneFrameCallback;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import java.nio.ByteBuffer;

public class DataParser implements IDataParser {

    private static final byte FH = (byte) 0xfb;
    ByteBuffer buffer = ByteBuffer.allocate(8192 + 128);

    public void setBufferSize(int maxSize) {
        if (buffer.capacity() < maxSize ) {
            buffer = ByteBuffer.allocate(maxSize);
        }
        buffer.clear();
        SuperLogUtil.sd("data parser, buffer size: " + buffer.capacity());
    }

    public void addData(byte[] data, OnParseOneFrameCallback onParseOneFrameCallback) {
        // mark() -> reset()
        // compact() -> 旧数据移动到首部，可重新读写
        //初始化（allocate）–> 写入数据（read / put）–> 转换为写出模式（flip）–> 写出数据（get）
        // –> 转换为写入模式（compact）–> 写入数据（read / put）
        if (buffer.remaining() < data.length) {
            buffer.compact();
        }
        //SuperLogUtil.sd("remain: " + buffer.remaining() + ", size: " + buffer.capacity() + ", pos: " + buffer.position());
        buffer.put(data);
        buffer.flip();
        parse(onParseOneFrameCallback);
    }

    // 帧头 长度 关键字 数据 校验和
    private void parse(OnParseOneFrameCallback onParseOneFrameCallback) {
        if (buffer.remaining() <= 0) return;
        // step 1: 找到帧头
        byte header;
        do {
            buffer.mark();
            header = buffer.get();
        }while (header != FH && buffer.remaining()>0);
        if (header != FH) { // 没找到帧头
            buffer.compact();
            return;
        }
        // 找到帧头 后续无数据
        if (buffer.remaining() == 0) {
            buffer.reset();
            buffer.compact();
            return;
        }
        // 找到帧头 后续数据不够一帧
        int dataLen = buffer.get();
        if (buffer.remaining() < dataLen-1) {
            buffer.reset();
            buffer.compact();
            return;
        }

        // step 2: 获取一帧数据
        byte[] data = new byte[dataLen-1]; // 不包括校验和
        buffer.get(data, 0, data.length-1);

        byte checkSum = buffer.get();

        if (checkSum == calculateCheck((byte) (dataLen), data)) {
            // 校验通过
            byte[] frame = new byte[dataLen+1];
            frame[0] = FH;
            frame[1] = (byte) dataLen;
            for (int i = 0; i < data.length; i++) {
                frame[2+i] = data[i];
            }
            frame[dataLen] = checkSum;
            /*if (BuildConfig.DEBUG) {
                SuperLogUtil.sd("解析到数据帧：" + frame.length);
            }*/
            // 添加帧
            if(onParseOneFrameCallback != null) {
                onParseOneFrameCallback.onParseOneFrame(frame);
            }
        }
        parse(onParseOneFrameCallback);
    }

    private byte calculateCheck(byte initCheck, byte[] data) {
        byte checkSum = initCheck;
        for (int i = 0; i < data.length; i++) {
            checkSum += data[i];
        }
        return checkSum;
    }
}
