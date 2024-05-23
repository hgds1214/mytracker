package com.zeus.tec.device.usbserial;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class USBSerialManager implements SerialInputOutputManager.Listener {
    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;

    private enum UsbPermission { Unknown, Requested, Granted, Denied }

    private int deviceId, portNum;
    private final int baudRate = 115_200;
    private boolean withIoManager = true;
    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort usbSerialPort;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private boolean connected = false;

    private List<DeviceListItem> deviceListItems = new ArrayList<>();
    private ParserCenter dataParserCenter = new ParserCenter();
    private List<USBStateChangeListener> stateChangedListenerList = new ArrayList<>();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        ? UsbPermission.Granted : UsbPermission.Denied;
                connect(context);
            }
        }
    };

    private static class Holder {
        static USBSerialManager sUSBSerialManager = new USBSerialManager();
    }
    public static USBSerialManager getInstance() {
        return Holder.sUSBSerialManager;
    }

    public void refresh(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        deviceListItems.clear();
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++) {
                    deviceListItems.add(new DeviceListItem(device, port, driver));
                }
            }
        }
        if (getValidDevice() != null) {
            notifyDeviceAttached();
        }
    }

    private void notifyDeviceAttached() {
        synchronized (stateChangedListenerList) {
            for (USBStateChangeListener l : stateChangedListenerList) {
                l.onDeviceAttached();
            }
        }
    }

    public Status start(Context context) {
        try {
            context.registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
        if(usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted) {
            return connect(context);
        }
        SuperLogUtil.sd("连接失败: 没有权限");
        return Status.newFail("连接失败: 没有权限");
    }

    public void release(Context context) {
        if(connected) {
            SuperLogUtil.sd("release usb");
            disconnect();
        }
        try {
            context.unregisterReceiver(broadcastReceiver);
        }catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
    }

    public void sendFrame(ParserCenter.FrameWrapper frameWrapper) {
        if (frameWrapper == null) return;
        dataParserCenter.sendFrame(frameWrapper);
    }

    public boolean send(byte[] data) {
        if(!connected) {
            ToastUtils.showLong("发送失败，没有连接USB设备");
            //disconnect();
            return false;
        }
        try {
            if (data.length > 0) {
                usbSerialPort.write(data, WRITE_WAIT_MILLIS);
            }
            return true;
        } catch (Exception e) {
            LogUtils.e("发送数据失败，没有连接USB设备");
            //disconnect();
            return false;
        }
    }
    private byte[] read() {
        if(!connected) {
            ToastUtils.showLong("读取失败，没有连接USB设备");
            return null;
        }
        try {
            byte[] buffer = new byte[8192];
            int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);
            return Arrays.copyOf(buffer, len);
        } catch (IOException e) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            e.printStackTrace();
            LogUtils.e("设备连接丢失: " + e.getMessage());
            SuperLogUtil.sd("设备连接丢失: " + e.getMessage());
            disconnect();
        }
        return null;
    }


    private Status connect(Context context) {
        SuperLogUtil.sd("connecting......");
        DeviceListItem deviceListItem = getValidDevice();
        if (deviceListItem == null) {
            SuperLogUtil.sd("没有连接设备");
            return Status.newFail("没有连接设备");
        }
        deviceId = deviceListItem.device.getDeviceId();
        portNum = deviceListItem.port;

        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values()) {
            if (v.getDeviceId() == deviceId) device = v;
        }
        if(device == null) {
            SuperLogUtil.sd("连接失败: 没找到设备");
            return Status.newFail("连接失败: 没找到设备");
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            SuperLogUtil.sd("连接失败: 没有安装设备驱动");
            return Status.newFail("连接失败: 没有安装设备驱动");
        }
        if(driver.getPorts().size() < portNum) {
            SuperLogUtil.sd("连接失败: 设备没有足够端口号");
            return Status.newFail("连接失败: 设备没有足够端口号");
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_GRANT_USB), flags);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return Status.newRequestPermission();
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                SuperLogUtil.sd("连接失败: 没有权限");
                return Status.newFail("连接失败: 没有权限");
            } else {
                SuperLogUtil.sd("连接失败: 打开失败");
                return Status.newFail("连接失败: 打开失败");
            }
        }

        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE);
            if(withIoManager) {
                usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
                usbIoManager.start();
            }
            if (dataParserCenter != null ) {
                dataParserCenter.setBufferSize(usbSerialPort.getReadEndpoint().getMaxPacketSize());
            }
            connected = true;
            notifyStateChanged(true);
            return Status.newSuccess("已连接");
        } catch (Exception e) {
            SuperLogUtil.sd("连接失败: " + e.getMessage());
            disconnect();
            return Status.newFail("连接失败: " + e.getMessage());
        }
    }

    private DeviceListItem getValidDevice() {
        if (deviceListItems.size() <= 0) {
            return null;
        }
        DeviceListItem deviceListItem = deviceListItems.get(0);
        if (deviceListItem .device != null ) {
            return deviceListItem;
        }
        return null;
    }

    private void disconnect() {
        connected = false;
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            if (usbSerialPort != null) usbSerialPort.close();
        } catch (IOException ignored) {}
        usbSerialPort = null;

        notifyStateChanged(false);
    }

    public boolean isConnected() {
        return connected;
    }

    public void addStateChangedListener(USBStateChangeListener listener) {
        if (listener == null) return;
        synchronized (stateChangedListenerList) {
            if (this.stateChangedListenerList.contains(listener)) return;
            this.stateChangedListenerList.add(listener);
        }
    }

    public void removeStateChangedListener(USBStateChangeListener listener) {
        if (listener == null) return;
        synchronized (stateChangedListenerList) {
            this.stateChangedListenerList.remove(listener);
        }
    }

    private void notifyStateChanged(boolean connected) {
        synchronized (stateChangedListenerList) {
            for (USBStateChangeListener l : stateChangedListenerList) {
                if (connected) {
                    l.onConnected();
                } else {
                    l.onDisconnected();
                }
            }
        }
    }

    @Override
    public void onNewData(byte[] data) {
        /*if (BuildConfig.DEBUG) {
            LogUtils.d("onNewData: ", Thread.currentThread().getName());
            SuperLogUtil.sd("接收到串口数据: " + data.length + ", " + Thread.currentThread().getName());
        }*/
        dataParserCenter.addData(data, null);
    }

    @Override
    public void onRunError(Exception e) {
        SuperLogUtil.sd("onRunError usb连接丢失: " + e.getMessage());
        disconnect();
    }

    public EnumSet<UsbSerialPort.ControlLine> getControlLines() {
        if (!connected) return null;
        try {
           return usbSerialPort.getControlLines();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
        return null;
    }

    public static class Status {
        public int code; // -1 失败 0 成功 1 请求权限
        public String message;

        public Status(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public static Status newFail(String message) {
            return new Status(-1, message);
        }
        public static Status newSuccess(String message) {
            return new Status(0, message);
        }
        public static Status newRequestPermission() {
            return new Status(1, "请求权限中");
        }

    }

    public interface USBStateChangeListener {
        void onDeviceAttached();
        void onConnected();
        void onDisconnected();
    }
}
