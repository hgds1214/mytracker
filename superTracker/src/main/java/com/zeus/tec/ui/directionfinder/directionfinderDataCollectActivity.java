package com.zeus.tec.ui.directionfinder;

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
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.BuildConfig;
import com.zeus.tec.R;

import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo;
import com.zeus.tec.model.directionfinder.directionfinderPointRecordInfo;
import com.zeus.tec.ui.directionfinder.Apater.directionfinderPointRecordApater;
import com.zeus.tec.ui.directionfinder.util.BLEDevice;
import com.zeus.tec.ui.directionfinder.util.BLEManager;
import com.zeus.tec.ui.directionfinder.util.LVDevicesAdapter;
import com.zeus.tec.ui.directionfinder.util.OnBleConnectListener;
import com.zeus.tec.ui.directionfinder.util.OnDeviceSearchListener;
import com.zeus.tec.ui.directionfinder.util.TypeConversion;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.leida.util.MesseagWindows;
import com.zeus.tec.databinding.ActivityDirectionfinderDataCollectBinding;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class directionfinderDataCollectActivity extends AppCompatActivity implements View.OnClickListener {

    //region 全局变量
    public static final String SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455";  //蓝牙通讯服务
    public static final String READ_UUID = "49535343-1e4d-4bd9-ba61-23c647249616";  //读特征
    public static final String WRITE_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3";  //写特征
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

    private static final String TAG = "BLEMain";
    private Context mContext;
    private LVDevicesAdapter lvDevicesAdapter;
    private BLEManager bleManager;
    private directionfinderDataCollectActivity.BLEBroadcastReceiver bleBroadcastReceiver;
    private BluetoothDevice curBluetoothDevice;  //当前连接的设备
    //当前设备连接状态
    private boolean curConnState = false;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ListView lvDevices;
    private LinearLayout lldevice;
    private LinearLayout layprogramparamter;
    private ListView listpoint;

    private long drillId = 0;
    private int funType = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private dirctionfinderDrillHoleInfo drillInfo = null;
    private directionfinderPointRecordApater directionfinderPointRecordApater;

    //endregion
    public ActivityDirectionfinderDataCollectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //用于多个其他界面进入不同状态的该界面
        drillId = getIntent().getLongExtra(KEY_DRILL_INFO_ID, 0);
        funType = getIntent().getIntExtra(KEY_TYPE_MERGE, 0);
        if (drillId != 0) {
            drillInfo = TrackerDBManager.getOnedirctionfinderDrillHoleInfo(drillId);
            directionfinderPointRecordApater = new directionfinderPointRecordApater(directionfinderDataCollectActivity.this);
            directionfinderPointRecordApater.DirectionfinderPointRecordInfo = TrackerDBManager.getrecordBydirectionfinderInfoId(drillId);
            downloadNumber = directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size();

        }
        if (BuildConfig.DEBUG) {
            LogUtils.d(drillId, drillInfo);
        }

        if (drillInfo == null) {
            ToastUtils.showLong("错误工程信息，请重试");
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        binding = ActivityDirectionfinderDataCollectBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());
        if (downloadNumber == 0) {
            findViewById(R.id.tv_not_record).setVisibility(View.VISIBLE);
        }
        mContext = directionfinderDataCollectActivity.this;
        //android 6.0后 蓝牙扫描需要申请动态权限
        requestPermission();
        initView();
        initProjectParam();
        initBLE();
        initListener();
        initBLEBroadcastReceiver();
        // setContentView(R.layout.activity_directionfinder_data_collect);
    }

    private void initView() {
        lvDevices = binding.lvDevices;
        lldevice = binding.llDevices;
        layprogramparamter = binding.layProgramParamter;
        binding.ivStep1.setState(3);
        listpoint = binding.listPoint;

        listpoint.setAdapter(directionfinderPointRecordApater);
        binding.tvStep1Text.setText("设备未连接,请先连接设备");
    }

    private void initProjectParam() {
        try {
            binding.tvProjectName.setText(drillInfo.projectName);
            binding.tvCreatTime.setText(drillInfo.drillHoleId);
            binding.tvPointDistance.setText(String.valueOf(drillInfo.designDirection));
            binding.tvPointNumber.setText(String.valueOf(drillInfo.designAngle));
            binding.tvOverLayNumber.setText(String.valueOf(drillInfo.jacketLength));
            binding.tvDelay.setText(String.valueOf(drillInfo.holeX));
            binding.tvAmp11.setText(String.valueOf(drillInfo.holeY));
            binding.tvFrenquence1.setText(String.valueOf(drillInfo.holeZ));
            binding.tvTImeSpace.setText(String.valueOf(drillInfo.compassCalibrationError));
            binding.tvGyro.setText(String.valueOf(drillInfo.presetHeading));
            binding.tvIsDownload.setText(String.valueOf(drillInfo.laserLevelError));

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public static void launch(Context context, long drillInfoId) {
        launch(context, drillInfoId, 0);
    }

    public static void launch(Context context, long drillInfoId, int type) {
        Intent intent = new Intent(context, directionfinderDataCollectActivity.class);
        intent.putExtra(KEY_DRILL_INFO_ID, drillInfoId);
        intent.putExtra(KEY_TYPE_MERGE, type);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()) {
            case R.id.tv_Data_Download:
                if (binding.tvDataDownload.getText().toString().equals("连接设备")) {
                    layprogramparamter.setVisibility(View.GONE);
                    binding.layoutPointRecord.setVisibility(View.GONE);
                    binding.tvPointRecord.setVisibility(View.GONE);
                    binding.tvProgramParamter.setVisibility(View.GONE);
                    lldevice.setVisibility(View.VISIBLE);
                    binding.tvNotDevice.setVisibility(View.VISIBLE);
                    binding.tvDataDownload.setText("关闭连接");
                    searchBtDevice();
                } else if (binding.tvDataDownload.getText().toString().equals("关闭连接")) {
                    layprogramparamter.setVisibility(View.VISIBLE);
                    lldevice.setVisibility(View.GONE);
                    binding.tvPointRecord.setVisibility(View.VISIBLE);
                    binding.tvProgramParamter.setVisibility(View.VISIBLE);
                    binding.tvDataDownload.setText("连接设备");
                }
                break;
            case R.id.tv_point_record:
                if (binding.layoutPointRecord.getVisibility() == View.GONE) {
                    binding.layProgramParamter.setVisibility(View.GONE);
                    binding.layoutPointRecord.setVisibility(View.VISIBLE);
                    binding.tvPointRecord.setBackgroundResource(R.drawable.btn_collect_bg);
                    binding.tvProgramParamter.setBackgroundResource(R.drawable.btn_finish_bg);
                    binding.tvPointRecord.setTextColor(Color.WHITE);
                    binding.tvProgramParamter.setTextColor(Color.BLACK);
                }
                break;
            case R.id.tv_program_paramter:
                if (binding.layProgramParamter.getVisibility() == View.GONE) {
                    binding.layProgramParamter.setVisibility(View.VISIBLE);
                    binding.layoutPointRecord.setVisibility(View.GONE);
                    binding.tvPointRecord.setBackgroundResource(R.drawable.btn_finish_bg);
                    binding.tvProgramParamter.setBackgroundResource(R.drawable.btn_collect_bg);
                    binding.tvPointRecord.setTextColor(Color.BLACK);
                    binding.tvProgramParamter.setTextColor(Color.WHITE);
                }
                break;
            case R.id.tv_Point:
                if (curConnState) {
                    if (firstRecord) {
                        binding.startButton.setEnabled(true);
                        binding.tvNotRecord.setVisibility(View.GONE);
                    }
                    directionfinderPointRecordInfo recordInfo = new directionfinderPointRecordInfo();
                    recordInfo.directionfinderInfoId = drillId;
                    recordInfo.oritentionAngle = orientationAngle;
                    recordInfo.dipAngle = dipAngle;
                    recordInfo.relativeAngle = relativeAngle;
                    recordInfo.recordTime = sdf.format(new Date(System.currentTimeMillis()));
                    directionfinderPointRecordApater.addPointRecord(recordInfo);
                }
                else {
                    Toast.makeText(mContext, "请先进入寻北模式", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.start_button:
                if (directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size() > 0) {
                    try {
                        int changeNumber = directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size() - downloadNumber;
                        if (changeNumber > 0) {
                            for (int i = 0; i < changeNumber; i++) {
                                TrackerDBManager.saveOrUpdate(directionfinderPointRecordApater.DirectionfinderPointRecordInfo.get(downloadNumber + i));
                            }
                            SimpleDateFormat sdfTmp = new SimpleDateFormat("yyyyMMdd-HHmmss");
                            String curTime =   sdfTmp.format(new Date(System.currentTimeMillis()));
                            String projectRoot = drillInfo.projectRoot;
                            if (drillInfo.dataPath.equals("")){
                                drillInfo.dataPath = projectRoot + File.separator+ curTime+".csv";
                            }
                            File pR = new File(drillInfo.dataPath);
                            if (pR.exists()) {
                                MesseagWindows.showMessageBox(this, "是否覆盖", "数据文件已存在，是否覆盖当前文件", new DialogCallback() {
                                    @Override
                                    public void onPositiveButtonClick() {
                                        pR.delete();
                                        try {
                                            pR.createNewFile();
                                            writeData(drillInfo.dataPath);
                                            if (drillInfo.zipPath.equals("")){
                                                drillInfo.zipPath = projectRoot+File.separator+curTime+"zip.zip";
                                            }
                                            zipFiles(drillInfo.zipPath,drillInfo.dataPath,drillInfo.livePhotos);
                                            Toast.makeText(directionfinderDataCollectActivity.this, changeNumber + "个测点数据保存成功", Toast.LENGTH_LONG).show();
                                            downloadNumber = directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size();
                                        }
                                        catch (Exception ex){
                                            ex.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onNegativeButtonClick() {

                                    }
                                });
                            }
                            else {
                                try {
                                    pR.createNewFile();
                                    writeData(drillInfo.dataPath);
                                    if (drillInfo.zipPath.equals("")){
                                        drillInfo.zipPath = projectRoot+File.separator+curTime+"zip.zip";
                                    }
                                    zipFiles(drillInfo.zipPath,drillInfo.dataPath,drillInfo.livePhotos);
                                    Toast.makeText(directionfinderDataCollectActivity.this, changeNumber + "个测点数据保存成功", Toast.LENGTH_LONG).show();
                                    downloadNumber = directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size();
                                }
                                catch (Exception exception){
                                    exception.printStackTrace();
                                }
                            }
                        } else if (changeNumber == 0) {
                            Toast.makeText(directionfinderDataCollectActivity.this, "测点数据已存在", Toast.LENGTH_LONG).show();
                        }
                    } catch
                    (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size() == 0) {
                    Toast.makeText(directionfinderDataCollectActivity.this, "测点数据不存在", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private  void  writeData (String csvFilePath){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            // 写入CSV文件头部
            writer.write("项目名称,钻孔编号,方向角,倾斜角,相关角,采集时间,检查员");
            writer.newLine();
            for (int i = 0; i < directionfinderPointRecordApater.DirectionfinderPointRecordInfo.size(); i++) {
                directionfinderPointRecordInfo  dirRecordInfo = directionfinderPointRecordApater.DirectionfinderPointRecordInfo.get(i);
                String line =  drillInfo.projectName +"," + drillInfo.drillHoleId +","+dirRecordInfo.oritentionAngle+","+dirRecordInfo.dipAngle+","+dirRecordInfo.relativeAngle+","+dirRecordInfo.recordTime+","+drillInfo.detector;
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param zipFilePath zip文件存放路径
     * @param sourceFilePaths 需要压缩的原文件路径
     */
    public static void zipFiles(String zipFilePath, String... sourceFilePaths) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(fos))) {
            byte[] buffer = new byte[1024];
            for (String sourceFilePath : sourceFilePaths) {
                File fileToZip = new File(sourceFilePath);
                try (FileInputStream fis = new FileInputStream(fileToZip);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {

                    // Add ZIP entry to output stream.
                    zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));

                    // Transfer bytes from the file to the ZIP file
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }

                    // Complete the entry
                    zipOut.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int downloadNumber = 0;
    private boolean firstDowmload = true;
    private boolean firstRecord = true;

    @SuppressLint({"HandlerLeak"})
    private Handler mHandler = new Handler() {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case START_DISCOVERY:
                    Toast.makeText(mContext, "开始搜索设备。。。", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "开始搜索设备...");
                    break;

                case STOP_DISCOVERY:
                    Toast.makeText(mContext, "停止搜索设备。。。", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "停止搜索设备...");
                    break;

                case DISCOVERY_DEVICE:  //扫描到设备
                    BLEDevice bleDevice = (BLEDevice) msg.obj;
                    String bleDeviceName = bleDevice.getBluetoothDevice().getName();
                    if (bleDeviceName != null) {
                        if (bleDeviceName.contains("YHZ")) {

                            lvDevicesAdapter.addDevice(bleDevice);
                            if (binding.tvNotDevice.getVisibility() == View.VISIBLE) {
                                binding.tvNotDevice.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;

                case SELECT_DEVICE:
                    //  tvName.setText(bluetoothDevice.getName());
                    // tvAddress.setText(bluetoothDevice.getAddress());
                    curBluetoothDevice = (BluetoothDevice) msg.obj;
                    binding.ivStep1.setState(1);
                    binding.tvStep1Text.setText("正在连接设备：" + curBluetoothDevice.getName());
                    Toast.makeText(mContext, "正在连接设备：" + curBluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                    bleManager.connectBleDevice(mContext, curBluetoothDevice, 15000, SERVICE_UUID, READ_UUID, WRITE_UUID, onBleConnectListener);
                    break;

                case CONNECT_FAILURE: //连接失败
                    binding.ivStep1.setState(3);
                    binding.tvStep1Text.setText("连接设备失败：" + curBluetoothDevice.getName());
                    Log.d(TAG, "连接失败");
                    // tvCurConState.setText("连接失败");
                    // curConnState = false;
                    break;

                case CONNECT_SUCCESS:  //连接成功
                    Log.d(TAG, "连接成功");
                    // tvCurConState.setText("连接成功");
                    curConnState = true;
                    binding.ivStep1.setState(2);
                    binding.tvStep1Text.setText("已连接：" + curBluetoothDevice.getName());
                    Toast.makeText(mContext, "连接成功", Toast.LENGTH_LONG).show();
                    binding.tvDataDownload.setText("连接设备");
                    binding.llDevices.setVisibility(View.GONE);
                    binding.layoutPointRecord.setVisibility(View.GONE);
                    binding.layProgramParamter.setVisibility(View.VISIBLE);
                    binding.tvPointRecord.setVisibility(View.VISIBLE);
                    binding.tvProgramParamter.setVisibility(View.VISIBLE);
                    //llDataSendReceive.setVisibility(View.VISIBLE);
                    // llDeviceList.setVisibility(View.GONE);
                    break;

                case DISCONNECT_SUCCESS:
                    Log.d(TAG, "断开成功");
                    // tvCurConState.setText("断开成功");
                    curConnState = false;
                    break;

                case SEND_FAILURE: //发送失败
                    byte[] sendBufFail = (byte[]) msg.obj;
                    String sendFail = TypeConversion.bytes2HexString(sendBufFail, sendBufFail.length);
                    //  tvSendResult.setText("发送数据失败，长度" + sendBufFail.length + "--> " + sendFail);
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
                    receiveMessage(recBufSuc);

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

    private byte[] angelByte = new byte[2];
    double orientationAngle = 0;
    double dipAngle = 0;
    double relativeAngle = 0;
    double elct = 0;
    boolean firstTime = true;

    private void receiveMessage(byte[] recBufSuc) {
        if (recBufSuc[0] == 0x5a && (recBufSuc[1] & 0xFF) == 0xa5) {
            if (recBufSuc[2] == 0x0a) {
                if (recBufSuc[3] == 0x0d) {
                    angelByte[0] = recBufSuc[4];
                    angelByte[1] = recBufSuc[5];
                    orientationAngle = ((double) ConvertCode.getushort(angelByte, ByteOrder.BIG_ENDIAN)) / 100;

                    angelByte[0] = recBufSuc[6];
                    angelByte[1] = recBufSuc[7];
                    dipAngle = ((double) ConvertCode.getint16(angelByte, ByteOrder.BIG_ENDIAN)) / 100;

                    angelByte[0] = recBufSuc[8];
                    angelByte[1] = recBufSuc[9];
                    relativeAngle = ((double) ConvertCode.getint16(angelByte, ByteOrder.BIG_ENDIAN)) / 100;

                    angelByte[0] = recBufSuc[10];
                    angelByte[1] = recBufSuc[11];
                    elct = ((double) ConvertCode.getint16(angelByte, ByteOrder.BIG_ENDIAN)) / 1000;
                    binding.tv24.setText(String.valueOf(relativeAngle));
                    binding.tv23.setText(String.valueOf(dipAngle));
                    binding.tv21.setText(String.valueOf(elct));
                    binding.tv22.setText(String.valueOf(orientationAngle));
                    if (!binding.tvStatus.getText().equals("寻北完成")){
                        binding.tvStatus.setText("寻北完成");
                    }
                    if (binding.tvCountTime.getText().equals("00:00:00")&&firstTime)
                    {
                        firstTime = false;
                        long currentTime = System.currentTimeMillis();
                        binding.tvCountTime.setStartTime(currentTime);
                        binding.tvCountTime.start();
                    }

                }
            } else if (recBufSuc[2] == 0x05 && recBufSuc[3] == 0x06) {
                switch (recBufSuc[4]) {
                    case 0x01:
                        binding.tvStatus.setText("开始启动");
                        break;
                    case 0x02:
                        binding.tvStatus.setText("正在寻北");
                        break;
                    case 0x03:
                        long currentTime = System.currentTimeMillis();
                        binding.tvCountTime.setStartTime(currentTime);
                        binding.tvCountTimeLabel.setText("运行时间");
                        binding.tvCountTime.start();
                        binding.tvStatus.setText("寻北完成");
                        break;
                    case 0x05:
                        binding.tvStatus.setText("退出测量");
                        break;
                    case 0x06:
                        binding.tvStatus.setText("退出并保存");
                        break;
                    case 0x07:
                        binding.tvStatus.setText("退出不保存");
                        break;
                    default:
                        binding.tvStatus.setText("启动失败");
                        break;
                }
            } else if (recBufSuc[2] == 0x06 && recBufSuc[3] == 0x07) {
                int sec = (recBufSuc[4] & 0xff) * 256 + (int) (recBufSuc[5] & 0xff);
                int min = sec / 60;
                sec = sec % 60;
                if (!binding.tvStatus.getText().equals("正在寻北"))
                {
                    binding.tvStatus.setText("正在寻北");
                }
                binding.tvCountTimeLabel.setText("寻北时间");
                String secTime = String.valueOf(sec);
                if (sec < 10) {
                    secTime = "0" + sec;
                }
                String findTime = "00:" + "0" + min + ":" + secTime;
                binding.tvCountTime.setText(findTime);
            } else if (recBufSuc[2] == 0x02 && recBufSuc[3] == 0x06) {
                if (recBufSuc[4] == 0x01) {
                    binding.tvStatus.setText("启动激光");
                } else if (recBufSuc[4] == 0x01) {
                    binding.tvStatus.setText("关闭激光");
                }
            }
        }
    }

    private String getAngleByByte(byte[] angelByte) {
        int integer = (int) angelByte[0];
        int decimal = angelByte[1];
        String angle = integer + "." + decimal;
        return angle;
    }

    private void initBLE() {
        //列表适配器
        lvDevicesAdapter = new LVDevicesAdapter(directionfinderDataCollectActivity.this);
        lvDevices.setAdapter(lvDevicesAdapter);
        bleManager = new BLEManager();
        if (!bleManager.initBle(mContext)) {
            Log.d(TAG, "该设备不支持低功耗蓝牙");
            Toast.makeText(mContext, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        } else {
//            if(!bleManager.isEnable()){
//                //去打开蓝牙
//                bleManager.openBluetooth(mContext,false);
//            }
        }
    }

    private void initListener() {
        binding.tvDataDownload.setOnClickListener(this);
        binding.tvPointRecord.setOnClickListener(this);
        binding.tvPoint.setOnClickListener(this);
        binding.tvProgramParamter.setOnClickListener(this);
        binding.startButton.setOnClickListener(this);
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
            //因为服务发现成功之后，才能通讯，所以在成功发现服务的地方表示连接成功
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

    /**
     * 注册广播
     */
    private void initBLEBroadcastReceiver() {
        //注册广播接收
        bleBroadcastReceiver = new BLEBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//手机蓝牙状态监听
        registerReceiver(bleBroadcastReceiver, intentFilter);
    }

    /**
     * 蓝牙广播接收器
     */
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


    final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();


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
            if (binding.tvNotDevice.getVisibility()==View.VISIBLE){
                binding.ivStep1.setState(3);
                binding.tvStep1Text.setText("扫描设备超时");
            }
            else if (binding.tvNotDevice.getVisibility()==View.GONE){
                binding.ivStep1.setState(2);
                binding.tvStep1Text.setText("扫描成功");
            }
            mHandler.sendMessage(message);
        }
    };

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