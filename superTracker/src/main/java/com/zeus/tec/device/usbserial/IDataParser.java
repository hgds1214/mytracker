package com.zeus.tec.device.usbserial;

import com.zeus.tec.device.tracker.Frame;

public interface IDataParser {
    void addData(byte[] data, OnParseOneFrameCallback onParseOneFrameCallback);
    void setBufferSize(int maxSize);
}
