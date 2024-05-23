package com.zeus.tec.device.usbserial;

public interface OnParseOneFrameCallback {
    void onParseOneFrame(byte[] frame);
}