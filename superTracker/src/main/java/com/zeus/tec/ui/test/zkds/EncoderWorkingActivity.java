package com.zeus.tec.ui.test.zkds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityEncoderWorkingBinding;
import com.zeus.tec.model.leida.MergeCache;
import com.zeus.tec.ui.directionfinder.util.BLEDevice;
import com.zeus.tec.ui.directionfinder.util.BLEManager;
import com.zeus.tec.ui.directionfinder.util.LVDevicesAdapter;
import com.zeus.tec.ui.directionfinder.util.OnBleConnectListener;
import com.zeus.tec.ui.directionfinder.util.OnDeviceSearchListener;
import com.zeus.tec.ui.directionfinder.util.TypeConversion;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.utils.log.SuperLogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class EncoderWorkingActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityEncoderWorkingBinding binding;
    private byte[] storgebuffer = new byte[2880000];
    private int indexbuffer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEncoderWorkingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mContext = EncoderWorkingActivity.this;
        superLogUtil = new SuperLogUtil(this);
        if (BuildConfig.DEBUG) {
        }
        requestPermission();
        initView();
        initBLE();
        initListener();
        initBLEBroadcastReceiver();
        calWheelDepth();
    }

    private void initView() {
        lvDevices = binding.lvDevices;
        lldevice = binding.llDevices;
        binding.ivStep1.setState(3);
        binding.tvStep1Text.setText("设备未连接,请先连接设备");
    }

    private com.zeus.tec.ui.directionfinder.Apater.directionfinderPointRecordApater directionfinderPointRecordApater;
    private static final String TAG = "BLEMain";
    private Context mContext;
    private LVDevicesAdapter lvDevicesAdapter;
    private BLEManager bleManager;
    private EncoderWorkingActivity.BLEBroadcastReceiver bleBroadcastReceiver;
    private BluetoothDevice curBluetoothDevice;  //当前连接的设备

    //region 全局变量
//    public static final String SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34";  //蓝牙通讯服务
//    public static final String READ_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";  //读特征
//    public static final String WRITE_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";  //写特征
    public static final String SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34";  //蓝牙通讯服务
    public static final String READ_UUID = "0000fff1-0000-0000-0000-000000000000";  //读特征
    public static final String WRITE_UUID = "0000fff2-0000-0000-0000-000000000000";  //写特征
    public static final String KEY_DRILL_INFO_ID = "DRILL_INFO_ID";
    public static final String KEY_TYPE_MERGE = "TYPE_MERGE";

    private static final int CONNECT_SUCCESS = 0x01;
    private static final int CONNECT_FAILURE = 0x02;
    private static final int DISCONNECT_SUCCESS = 0x03;
    private static final int SEND_SUCCESS = 0x04;
    private static final int SEND_FAILURE = 0x05;
    private static final int RECEIVE_SUCCESS = 0x06;
    private static final int RECEIVE_FAILURE = 0x07;
    private static final int START_DISCOVERY = 0x08;
    private static final int STOP_DISCOVERY = 0x09;
    private static final int DISCOVERY_DEVICE = 0x0A;
    private static final int DISCOVERY_OUT_TIME = 0x0B;
    private static final int SELECT_DEVICE = 0x0C;
    private static final int BT_OPENED = 0x0D;
    private static final int BT_CLOSED = 0x0E;

    private ListView lvDevices;
    private LinearLayout lldevice;
    private LinearLayout layprogramparamter;
    private ListView listpoint;
//endregion

    private void initBLE() {
        //列表适配器
        lvDevicesAdapter = new LVDevicesAdapter(EncoderWorkingActivity.this);
        lvDevices.setAdapter(lvDevicesAdapter);
        bleManager = new BLEManager();
        if (!bleManager.initBle(mContext)) {
            Log.d(TAG, "该设备不支持低功耗蓝牙");
            Toast.makeText(mContext, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        }
        else
        {
        }
    }
    private void calWheelDepth() {
        wheelDiameter = Integer.parseInt(String.valueOf(binding.tv21.getText()));
        int digit = getdigit(wheelDiameter, 1);
        WheelDiameterStr = String.valueOf(wheelDiameter);
        for (int i = 0; i < 4 - digit; i++) {
            WheelDiameterStr = "0" + WheelDiameterStr;
        }
        num2 = 0.001 * wheelDiameter;
    }
    int sampleFrenquence = 50;
    private int calFrenquenceDepth() {
        return Integer.parseInt(String.valueOf(binding.tvFrenquenceDepth.getText()));
    }
    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()) {
            case R.id.tv_binding_device:
                if (binding.tvBindingDevice.getText().toString().equals("连接设备")) {
                    superLogUtil.d("连接设备...");
                    lldevice.setVisibility(View.VISIBLE);
                    binding.tvNotDevice.setVisibility(View.VISIBLE);
                    binding.tvBindingDevice.setText("关闭连接");
                    searchBtDevice();
                    break;
                } else if (binding.tvBindingDevice.getText().toString().equals("关闭连接")) {
                    bleManager.disConnectDevice();
                    ToastUtils.showLong("设备连接已断开！");
                    lldevice.setVisibility(View.GONE);
                    binding.tvNotDevice.setVisibility(View.GONE);
                    binding.tvBindingDevice.setText("连接设备");
                    break;
                }
            case R.id.start_button:
                String msg = "5a011505000000001b";
                bleManager.sendMessage(msg);
                break;
            //  case  R.id.
            case R.id.tv_program_paramter:
            {
                try {
                    calWheelDepth();
                } catch (Exception exception) {
                    break;
                }
                ToastUtils.showLong("设置成功");
                break;
            }
            case R.id.tv_begin_working: {
                ToastUtils.showLong("开始工作");
                String rootPath = PathUtils.getExternalAppFilesPath() + File.separator + "wirelessEncoderData";
                if (!FileUtils.createOrExistsDir(rootPath)) {
                    LogUtils.e("创建文件失败：" + rootPath);
                    ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                    return;
                }
                String strFileName = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    strFileName = rootPath + File.separator + LocalDateTime.now().toString() + ".ini";
                }
                if (!FileUtils.createOrExistsFile(strFileName)) {
                    LogUtils.e("创建文件失败：" + strFileName);
                    ToastUtils.showLong("创建文件失败，请检查磁盘空间后重试!");
                    return;
                }
                try {
                    fos = new FileOutputStream(strFileName);
                } catch (Exception exception){
                    ToastUtils.showLong(exception.getMessage());
                }
                currentStatus = Status_Working;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    currentTimeMillis = System.currentTimeMillis();
                }
                binding.tvStopWorking.setEnabled(true);
                binding.tvBeginWorking.setEnabled(false);
                break;
            }
            case R.id.tv_stop_working: {
                currentStatus = Status_Sleeping;
                pointIndex = 0;
                binding.tvStopWorking.setEnabled(false);
                binding.tvBeginWorking.setEnabled(true);
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.tv_set_frenquence:
            {
                sampleFrenquence = calFrenquenceDepth();
                int CRC = sampleFrenquence + 8;
                String sendMsg = "5a01040300" + Integer.toHexString(sampleFrenquence) + Integer.toHexString(CRC);
                bleManager.sendMessage(sendMsg);
                break;
            }
        }
    }

    String WheelDiameterStr;

    private int getdigit(int WheelDiameter, int digit) {
        double num1 = Math.pow(10, digit);
        if (WheelDiameter / num1 >= 1) {
            digit++;
            return getdigit(WheelDiameter, digit);
        } else {
            return digit;
        }
    }

    @SuppressLint("NewApi")
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    FileOutputStream fos;
    private String beginDate = null;
    private int wheelDiameter = 0;
    long currentTimeMillis;

    private class BLEBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_STARTED)) { //开启搜索
                Message message = new Message();
                message.what = START_DISCOVERY;
                mHandler.sendMessage(message);
            } else if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {//完成搜素
                Message message = new Message();
                message.what = STOP_DISCOVERY;
                mHandler.sendMessage(message);
            } else if (TextUtils.equals(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {   //系统蓝牙状态监听
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (state == BluetoothAdapter.STATE_OFF) {
                    Message message = new Message();
                    message.what = BT_CLOSED;
                    mHandler.sendMessage(message);
                } else if (state == BluetoothAdapter.STATE_ON) {
                    Message message = new Message();
                    message.what = BT_OPENED;
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    String[] zeroStrList = {"00000", "0000", "000", "00", "0", ""};
    private byte[] angelByte = new byte[2];
    double orientationAngle = 0;
    double dipAngle = 0;
    double relativeAngle = 0;
    double elct = 0;
    boolean firstTime = true;
    private int Status_Working = 0x01;
    private int Status_Sleeping = 0x02;
    private int currentStatus = Status_Sleeping;
    private int pointIndex = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:");
    private double num2 = 0;
    String formattedDate;

    private void receiveMessage(byte[] recBufSuc) throws IOException {
        if (recBufSuc[0] == 0x5a && recBufSuc[1] == 0x01) {
            if (recBufSuc[2] == 0x50 && recBufSuc[3] == 0x05) {
                int value = ((recBufSuc[4] & 0xFF) << 24) | ((recBufSuc[5] & 0xFF) << 16) | ((recBufSuc[6] & 0xFF) << 8) | (recBufSuc[7] & 0xFF);
                // double depth = (Math.PI * wheelDiameter * (double) value) / 360.0;
                if (currentStatus == Status_Working) {
                    currentTimeMillis = System.currentTimeMillis();
                    // 将毫秒时间戳转换为Date对象
                    Date resultDate = new Date(currentTimeMillis);
                    // 格式化Date对象为字符串
                    formattedDate = sdf.format(resultDate);
                    int digit = getdigit(value, 1);
                    String tmpStr;
                    //测试
                    if (pointIndex % 100 >= 10) {
                        tmpStr = formattedDate + pointIndex%100 + " " + pointIndex + " " + zeroStrList[digit - 1] + value + " " + WheelDiameterStr + " -90 100 0\n";
                    } else {
                        tmpStr = formattedDate + "0" + pointIndex%100 + " " + pointIndex + " " + zeroStrList[digit - 1] + value + " " + WheelDiameterStr + " -90 100 0\n";
                    }
                    fos.write(tmpStr.getBytes(StandardCharsets.UTF_8)); // 把字符串写入文件输出流
                    // currentTimeMillis = currentTimeMillis + 100;
                    pointIndex++;
                }
                double depth = (num2 * value) / 1000.0;
                binding.tv23.setText(String.format("%.2f", depth));
                return;
            } else if (recBufSuc[2] == 0x04 && recBufSuc[3] == 0x02) {
                ToastUtils.showLong("深度采样周期设置为"+sampleFrenquence+"ms");
                return;
            }
        }
    }

    @SuppressLint({"HandlerLeak"})
    private Handler mHandler = new Handler() {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_DISCOVERY:
                   // Toast.makeText(mContext, "开始搜索设备。。。", Toast.LENGTH_SHORT).show();
                    if (BuildConfig.DEBUG) superLogUtil.d("开始搜索设备...");
                    Log.d(TAG, "开始搜索设备...");
                    break;
                case STOP_DISCOVERY:
                   // Toast.makeText(mContext, "停止搜索设备。。。", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "停止搜索设备...");
                    break;
                case DISCOVERY_DEVICE:  //扫描到设备
                    BLEDevice bleDevice = (BLEDevice) msg.obj;
                    String bleDeviceName = bleDevice.getBluetoothDevice().getName();
                    if (bleDeviceName != null) {
                        if (bleDeviceName.contains("无线")) {
                            lvDevicesAdapter.addDevice(bleDevice);
                            if (binding.tvNotDevice.getVisibility() == View.VISIBLE) {
                                binding.tvNotDevice.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
                case SELECT_DEVICE:
                    curBluetoothDevice = (BluetoothDevice) msg.obj;
                    Toast.makeText(mContext, "正在连接设备：" + curBluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                    bleManager.connectBleDevice(mContext, curBluetoothDevice, 15000, SERVICE_UUID, READ_UUID, WRITE_UUID, onBleConnectListener);
                    break;
                case CONNECT_FAILURE: //连接失败
                    Log.d(TAG, "连接失败");
                    break;
                case CONNECT_SUCCESS:  //连接成功
                    Log.d(TAG, "连接成功");
                    // tvCurConState.setText("连接成功");
                    binding.ivStep1.setState(2);
                    binding.tvStep1Text.setText("已连接：" + curBluetoothDevice.getName());
                    Toast.makeText(mContext, "连接成功", Toast.LENGTH_LONG).show();
                    bleManager.sendMessage("5a0106020008");
                    break;
                case DISCONNECT_SUCCESS:
                    Log.d(TAG, "断开成功");
                    break;
                case SEND_FAILURE: //发送失败
                    byte[] sendBufFail = (byte[]) msg.obj;
                    String sendFail = TypeConversion.bytes2HexString(sendBufFail, sendBufFail.length);
                    break;
                case SEND_SUCCESS:  //发送成功
                    byte[] sendBufSuc = (byte[]) msg.obj;
                    String sendResult = TypeConversion.bytes2HexString(sendBufSuc, sendBufSuc.length);
                    // tvSendResult.setText("发送数据成功，长度" + sendBufSuc.length + "--> " + sendResult);
                    break;
                case RECEIVE_FAILURE: //接收失败
                    String receiveError = (String) msg.obj;
                    // tvReceive.setText(receiveError);
                    break;
                case RECEIVE_SUCCESS:  //接收成功
                    byte[] recBufSuc = (byte[]) msg.obj;
                    Log.d(TAG, recBufSuc.toString());
                    try {
                        receiveMessage(recBufSuc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String receiveResult = TypeConversion.bytes2HexString(recBufSuc, recBufSuc.length);
                    // tvReceive.setText("接收数据成功，长度" + recBufSuc.length + "--> " + receiveResult);
                    break;
                case BT_CLOSED:
                    Log.d(TAG, "系统蓝牙已关闭");
                    break;
                case BT_OPENED:
                    Log.d(TAG, "系统蓝牙已打开");
                    break;
            }
        }
    };
    //扫描结果回调
    private OnDeviceSearchListener onDeviceSearchListener = new OnDeviceSearchListener() {
        @Override
        public void onDeviceFound(BLEDevice bleDevice) {
            Message message = new Message();
            message.what = DISCOVERY_DEVICE;
            message.obj = bleDevice;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDiscoveryOutTime() {
            Message message = new Message();
            message.what = DISCOVERY_OUT_TIME;
            if (binding.tvNotDevice.getVisibility() == View.VISIBLE) {
            } else if
            (binding.tvNotDevice.getVisibility() == View.GONE) {
            }
            mHandler.sendMessage(message);
        }
    };

    private SuperLogUtil superLogUtil;

    private void initListener() {
        binding.tvBindingDevice.setOnClickListener(this);
        binding.startButton.setOnClickListener(this);
        binding.tvProgramParamter.setOnClickListener(this);
        binding.tvBeginWorking.setOnClickListener(this);
        binding.tvStopWorking.setOnClickListener(this);
        binding.tvSetFrenquence.setOnClickListener(this);
        if (BuildConfig.DEBUG) {
            binding.tvShowDebug.setVisibility(View.VISIBLE);
            binding.tvShowDebug.setOnClickListener(v -> {
                FeedbackUtil.getInstance().doFeedback();
                superLogUtil.show();
            });
        }
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BLEDevice bleDevice = (BLEDevice) lvDevicesAdapter.getItem(i);
                BluetoothDevice bluetoothDevice = bleDevice.getBluetoothDevice();
                if (bleManager != null) {
                    bleManager.stopDiscoveryDevice();
                }
                Message message = new Message();
                message.what = SELECT_DEVICE;
                message.obj = bluetoothDevice;
                mHandler.sendMessage(message);
            }
        });
    }

    private OnBleConnectListener onBleConnectListener = new OnBleConnectListener() {
        @Override
        public void onConnecting(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice) {
        }

        @Override
        public void onConnectSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
        }

        @Override
        public void onConnectFailure(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, String exception, int status) {
            Message message = new Message();
            message.what = CONNECT_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDisConnecting(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice) {
        }

        @Override
        public void onDisConnectSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            Message message = new Message();
            message.what = DISCONNECT_SUCCESS;
            message.obj = status;
            mHandler.sendMessage(message);
        }

        @Override
        public void onServiceDiscoverySucceed(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            //因为服务发现成功之后，才能通讯，所以在成功发现服务的地方表示连接成功
            Message message = new Message();
            message.what = CONNECT_SUCCESS;
            mHandler.sendMessage(message);
        }

        @Override
        public void onServiceDiscoveryFailed(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, String failMsg) {
            Message message = new Message();
            message.what = CONNECT_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReceiveMessage(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, byte[] msg) {
            Message message = new Message();
            message.what = RECEIVE_SUCCESS;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReceiveError(String errorMsg) {
            Message message = new Message();
            message.what = RECEIVE_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onWriteSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, byte[] msg) {
            Message message = new Message();
            message.what = SEND_SUCCESS;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onWriteFailure(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, byte[] msg, String errorMsg) {
            Message message = new Message();
            message.what = SEND_FAILURE;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReadRssi(BluetoothGatt bluetoothGatt, int Rssi, int status) {
        }

        @Override
        public void onMTUSetSuccess(String successMTU, int newMtu) {
        }

        @Override
        public void onMTUSetFailure(String failMTU) {
        }
    };

    private void initBLEBroadcastReceiver() {
        //注册广播接收
        bleBroadcastReceiver = new EncoderWorkingActivity.BLEBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//手机蓝牙状态监听
        registerReceiver(bleBroadcastReceiver, intentFilter);
    }

    //搜索设备
    private void searchBtDevice() {
        if (bleManager == null) {
            Log.d(TAG, "searchBtDevice()-->bleManager == null");
            return;
        }
        if (bleManager.isDiscovery()) { //当前正在搜索设备...
            bleManager.stopDiscoveryDevice();
        }
        if (lvDevicesAdapter != null) {
            lvDevicesAdapter.clear();  //清空列表
        }
        binding.tvStep1Text.setText("正在搜索设备。。。");
        binding.ivStep1.setState(1);
        //开始搜索
        bleManager.startDiscoveryDevice(onDeviceSearchListener, 15000);
    }

    //动态获取权限
    private void requestPermission() {
        //动态申请是否有必要看sdk版本哈
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

}
