package com.zeus.tec.device.usbserial;

import android.hardware.usb.UsbDevice;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

class DeviceListItem {
    public UsbDevice device;
    public int port;
    public UsbSerialDriver driver;

    public DeviceListItem(UsbDevice device, int port, UsbSerialDriver driver) {
        this.device = device;
        this.port = port;
        this.driver = driver;
    }
}