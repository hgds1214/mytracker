package com.zeus.tec.ui.maoganDataUpload;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.github.mikephil.charting.charts.Chart;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityMaoganMainBinding;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.ui.directionfinder.directionfinderDataCollectActivity;
import com.zeus.tec.ui.directionfinder.util.BLEDevice;
import com.zeus.tec.ui.directionfinder.util.BLEManager;
import com.zeus.tec.ui.directionfinder.util.LVDevicesAdapter;
import com.zeus.tec.ui.directionfinder.util.OnBleConnectListener;
import com.zeus.tec.ui.directionfinder.util.OnDeviceSearchListener;
import com.zeus.tec.ui.directionfinder.util.TypeConversion;
import com.zeus.tec.ui.leida.interfaceUtil.DialogCallback;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.leida.util.MesseagWindows;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MaoganMainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityMaoganMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_maogan_main);
        binding = ActivityMaoganMainBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(binding.getRoot());
        requestPermission();
        initView();
        //initProjectParam();
        initBLE();
        initListener();
        initBLEBroadcastReceiver();
    }

    //region /****全局变量*****/
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

    private ListView lvDevices;
    private LinearLayout lldevice;
    private LinearLayout layprogramparamter;
    private ListView listdata;

    private static final String TAG = "BLEMain";
    private Context mContext;
    private LVDevicesAdapter lvDevicesAdapter;
    private BLEManager bleManager;
    private BLEBroadcastReceiver bleBroadcastReceiver;
    private BluetoothDevice curBluetoothDevice;  //当前连接的设备

    private boolean curConnState = false;
    //private FileInputStream fis;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private String maogan_login_token;
    private String maogan_login_expireDate;

    String requestBodyStr;
    //endregion

    public class UpdataParam {
        @RequiresApi(api = Build.VERSION_CODES.O)
        UpdataParam() {
            vendorId = "WHCS";
            serialNo = "29834";
            machineId = "29845";
            pileNo = "001";
            fileName = "whcs-001";
            projectName = "wuhan-001";
            siteName = "test";
            position = "";
            pourTime = "2019-09-19 03:10:52";
            startTime = "2019-09-19 03:10:52";
            gpsValid = 0;
            gpsLongitude = 0;
            gpsLatitude = 0;
            passwayCount = 6;
            samplePoints = 1017;
            sampleInterval = 0.17;
            prePileLen = 0;
            exposedLen = 0;
            rodSpeed = 0;
            barSpeed = 0;
            pileDiameter = 0;
            aperture = 0;
            delayPoints = 0;
            magnification = 0;
            highFilter = 10;
            lowFilter = 500;
            sensorType = 0;
            sensorSensitive = 0;
            integralFlag = 1;
            boltType = 2;
            materialType = 1;
            mortarGrade = 0;
            surroundGrade = 0;
            sampleAutoSet = 1;
            sampleType = 1;
            sampleModel = 1;
            remarks = "";
        }

        String vendorId;
        String serialNo;
        String machineId;
        String pileNo;
        String fileName;
        String projectName;
        String siteName;
        String position;
        String pourTime;
        String startTime;
        byte gpsValid;
        double gpsLongitude;
        double gpsLatitude;

        byte passwayCount;
        int samplePoints;
        double sampleInterval;
        float prePileLen;
        float exposedLen;
        int rodSpeed;
        int barSpeed;
        int pileDiameter;
        int aperture;
        int delayPoints;
        int magnification;
        int highFilter;
        int lowFilter;
        byte sensorType;
        float sensorSensitive;
        byte integralFlag;
        byte boltType;
        byte materialType;
        byte mortarGrade;
        byte surroundGrade;
        byte sampleAutoSet;
        byte sampleType;
        byte sampleModel;
        String remarks;


    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final String Url_Get_Setting = "https://iqt.yxgswater.com:8000/insp/xczy/api/login";
    private final String Url_Updata_MaoganData = "https://iqt.yxgswater.com:8000/insp/xczy/api/maoganData";
    private final OkHttpClient client = new OkHttpClient();

    boolean isReceiveDataPackeage = false;
    byte[] buf;
    int dataFramePosition = 0;
    int dataPackeageCount = 0;
    long checkSum = 0;
    int dataPackeagelength = 0;
    List<byte[]> bufList = new ArrayList<>();
    int BLE_Maxsize_Packeage = 205;

    Map<Integer, Boolean> isCheck = new HashMap<>();
    private IMaoganDataUpdata iMaoganDataUpdata = new IMaoganDataUpdata() {
        @Override
        public void updataData(File dataPath) {
            FeedbackUtil.getInstance().doFeedback();
            MesseagWindows.showMessageBox(mContext, "上传文件", "是否上传文件", new DialogCallback() {
                @Override
                public void onPositiveButtonClick() {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(dataPath);
                        BufferedInputStream fis = new BufferedInputStream(fileInputStream);
                        // fis = new FileInputStream(dataPath);
                        UpdataParam updataParam = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            updataParam = new UpdataParam();
                        }
                        byte[] fileHeadbuf = new byte[48];
                        byte[] tmpBuff2 = new byte[2];
                        fis.read(fileHeadbuf);
                        assert updataParam != null;
                        updataParam.fileName = new String(fileHeadbuf, StandardCharsets.UTF_8).trim();
                        fis.read(new byte[8]);
                        fis.read(tmpBuff2);
                        fis.read(new byte[6]);
                        updataParam.passwayCount = (byte) (ByteBuffer.wrap(tmpBuff2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0XFF);
                        byte[] dataHeadbuf = new byte[512];
                        fis.read(dataHeadbuf);
                        MaoganFileHead maoganFileHead = parseFileHead(dataHeadbuf);
                        updataParam.machineId = String.valueOf(maoganFileHead.fileSysId);
                        updataParam.pileNo = updataParam.fileName.split("-")[updataParam.fileName.split("-").length - 1];
                        updataParam.projectName = updataParam.fileName;
                        updataParam.siteName = updataParam.fileName.split("-")[0];
                        // updataParam.position = maoganFileHead.peg_pos.trim();
                        updataParam.position = "2";
                        updataParam.pourTime = maoganFileHead.test_data.trim();
                        // if (updataParam.pourTime.equals("")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            updataParam.pourTime = sdf.format(new Date(System.currentTimeMillis()));
                        }

                        updataParam.startTime = updataParam.pourTime;
                        // updataParam.samplePoints = maoganFileHead.SampleNum;
                        updataParam.sampleInterval = maoganFileHead.samp_interval;
                        updataParam.prePileLen = maoganFileHead.peg_length;
                        updataParam.rodSpeed = maoganFileHead.waveSpeed;
                        updataParam.lowFilter = maoganFileHead.lp_freg * 1000;
                        updataParam.highFilter = maoganFileHead.hp_freq;

                        requestBodyStr = getRequestBodyObj(updataParam, fis).toString();
                        httpPostrequest("https://iqt.yxgswater.com:8000/insp/xczy/api/maoganData");


                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                @Override
                public void onNegativeButtonClick() {
                }
            });

        }

        @Override
        public void deleteData(File dataPath) {
            FeedbackUtil.getInstance().doFeedback();
            MesseagWindows.showMessageBox(mContext, "删除文件", "是否删除文件", new DialogCallback() {
                @Override
                public void onPositiveButtonClick() {
                    if (FileUtils.delete(dataPath)) {
                        refreshDataList();
                    } else {
                        ToastUtils.showLong("文件删除失败！");
                    }
                }

                @Override
                public void onNegativeButtonClick() {

                }
            });
        }

        @Override
        public void clickCheckBox(int position, boolean IsCheck) {
            isCheck.put(position, IsCheck);
        }
    };

    private JSONObject getRequestBodyObj(UpdataParam updataParam, BufferedInputStream bis) throws JSONException, IOException {
        JSONObject requestBodyObj = new JSONObject();
        requestBodyObj.put("vendorId", updataParam.vendorId);
        requestBodyObj.put("serialNo", "WHCS");
        requestBodyObj.put("vendorId", updataParam.vendorId);
        requestBodyObj.put("serialNo", updataParam.serialNo);
        requestBodyObj.put("pileNo", updataParam.pileNo);
        requestBodyObj.put("projectName", updataParam.projectName);
        requestBodyObj.put("fileName", updataParam.fileName);
        requestBodyObj.put("machineId", updataParam.machineId);
        requestBodyObj.put("siteName", updataParam.siteName);
        requestBodyObj.put("position", updataParam.position);
        requestBodyObj.put("pourTime", updataParam.pourTime);
        requestBodyObj.put("startTime", updataParam.startTime);
        requestBodyObj.put("gpsValid", updataParam.gpsValid);
        requestBodyObj.put("gpsLongitude", updataParam.gpsLongitude);
        requestBodyObj.put("gpsLatitude", updataParam.gpsLatitude);
        requestBodyObj.put("passwayCount", updataParam.passwayCount);
        requestBodyObj.put("samplePoints", updataParam.samplePoints);
        requestBodyObj.put("sampleInterval", updataParam.sampleInterval);
        requestBodyObj.put("prePileLen", updataParam.prePileLen);
        requestBodyObj.put("exposedLen", updataParam.exposedLen);
        requestBodyObj.put("rodSpeed", updataParam.rodSpeed);
        requestBodyObj.put("barSpeed", updataParam.barSpeed);
        requestBodyObj.put("pileDiameter", updataParam.pileDiameter);
        requestBodyObj.put("aperture", updataParam.aperture);
        requestBodyObj.put("delayPoints", updataParam.delayPoints);
        requestBodyObj.put("magnification", updataParam.magnification);
        requestBodyObj.put("highFilter", updataParam.highFilter);
        requestBodyObj.put("lowFilter", updataParam.lowFilter);
        requestBodyObj.put("sensorType", updataParam.sensorType);
        requestBodyObj.put("sensorSensitive", updataParam.sensorSensitive);
        requestBodyObj.put("integralFlag", updataParam.integralFlag);
        requestBodyObj.put("boltType", updataParam.boltType);
        requestBodyObj.put("materialType", updataParam.materialType);
        requestBodyObj.put("mortarGrade", updataParam.mortarGrade);
        requestBodyObj.put("surroundGrade", updataParam.surroundGrade);
        requestBodyObj.put("sampleAutoSet", updataParam.sampleAutoSet);
        requestBodyObj.put("sampleType", updataParam.sampleType);
        requestBodyObj.put("sampleModel", updataParam.sampleModel);
        requestBodyObj.put("remarks", updataParam.remarks);
        requestBodyObj.put("testData", gettestDataJsonAry(bis, updataParam));


        return requestBodyObj;
    }

    private JSONArray gettestDataJsonAry(BufferedInputStream fis, UpdataParam updataParam) throws JSONException, IOException {

        byte[] tmpBuff = new byte[4];
        JSONArray testDataJsonAry = new JSONArray();
        for (int i = 0; i < 6; i++) {
            JSONObject testDataJsonObj = new JSONObject();
            testDataJsonObj.put("sampleTime", updataParam.startTime);//采样时间
            testDataJsonObj.put("passwayNum", i + 1);//通道号
            JSONArray waveDataJsonAry = new JSONArray();
            fis.read(tmpBuff);
            float max = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            fis.read(tmpBuff);
            int max_x = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            fis.read(tmpBuff);
            int bConfirm = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            fis.read(tmpBuff);
            float sampleRate = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            fis.read(tmpBuff);
            float Cursor1 = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            fis.read(tmpBuff);
            float Cursor2 = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            fis.read(tmpBuff);
            float length = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            for (int j = 0; j < 1017; j++) {
                fis.read(tmpBuff);
                waveDataJsonAry.put(ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() / 100.0);
            }
            testDataJsonObj.put("waveData", waveDataJsonAry);
            testDataJsonAry.put(testDataJsonObj);
            //fis.read(dataSegBuf);
        }
        return testDataJsonAry;
    }

    private String getLoginJson(String name, String key) {
        return "{\"loginName\":\"" + name + "\",\"password\":\"" + key + "\"}";
    }

    public final Callback callHttp3 = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String responseMsg = Objects.requireNonNull(response.body()).string();
            String requestUrl = response.request().url().toString();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(responseMsg);
            } catch (Exception exception) {
                ToastUtils.showShort(exception.getMessage());
            }
            switch (requestUrl) {
                case Url_Get_Setting: {
                    try {
                        String code = jsonObject.getString("code");
                        String msg = jsonObject.getString("msg");
                        if (code.equals("0")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                            maogan_login_expireDate = jsonObject1.getString("expireDate");
                            maogan_login_token = jsonObject1.getString("token");

                            runOnUiThread(() -> refreshLoginStatus());

                            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                            //获取Editor对象的引用
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            //将获取过来的值放入文件
                            editor.putString("maogan_login_expireDate", maogan_login_expireDate);
                            editor.putString("maogan_login_token", maogan_login_token);
                            // 提交数据
                            editor.commit();
                        } else {
                            ToastUtils.showLong(code + ":" + msg);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case Url_Updata_MaoganData: {
                    try {
                        int code = jsonObject.getInt("code");
                        String msg = jsonObject.getString("msg");
                        if (code == 0) {
                            ToastUtils.showLong("上传成功");
                        } else {
                            ToastUtils.showLong(code + ":" + msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        }
    };

    public void httpPostrequest(String url) {
        switch (url) {
            case Url_Get_Setting: {
                try {
                    String json = getLoginJson("whrsm", "whrsm");
                    // JSON 媒体类型
                    MediaType JSON = MediaType.get("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url(url)  // 替换为你的 URL
                            .post(body)
                            .build();
                    client.newCall(request).enqueue(callHttp3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case Url_Updata_MaoganData: {

                if (maogan_login_token.equals("0")) {
                    ToastUtils.showLong("请先登陆账号，获取Token!");
                } else {
                    try {
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        RequestBody body = RequestBody.create(requestBodyStr, JSON);
                        Request request = new Request.Builder()
                                .url(url)  // 替换为你的 URL
                                .addHeader("token", maogan_login_token)
                                .post(body)
                                .build();
                        client.newCall(request).enqueue(callHttp3);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                break;
            }
        }

    }

    private void initView() {
        lvDevices = binding.lvDevices;
        lldevice = binding.llDevices;
        layprogramparamter = binding.layProgramParamter;
        binding.ivStep1.setState(3);
        listdata = binding.listData;
        //  listpoint.setAdapter(directionfinderPointRecordApater);
        binding.tvStep1Text.setText("设备未连接,请先连接设备");
        refreshDataList();
        initLoginStatus();
    }

    private void refreshLoginStatus() {
        if (maogan_login_expireDate.equals("未登录")) {
            binding.tvCountTimeLabel.setText("未登录");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                // 将时间字符串解析为 Date 对象
                Date date = dateFormat.parse(maogan_login_expireDate);
                // 转换为时间戳（毫秒）
                long timestamp = date.getTime();
                if (System.currentTimeMillis() - timestamp > 0) {
                    binding.tvCountTime.setText(maogan_login_expireDate.replace(" ", System.lineSeparator()));
                    binding.tvCountTimeLabel.setText("登陆已过期");
                    binding.tvCountTimeLabel.setTextColor(Color.RED);
                } else {
                    binding.tvCountTime.setText(maogan_login_expireDate.replace(" ", System.lineSeparator()));
                    binding.tvCountTimeLabel.setText("登陆有效期");
                    binding.tvCountTimeLabel.setTextColor(Color.GREEN);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void initLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        maogan_login_expireDate = sharedPreferences.getString("maogan_login_expireDate", "未登陆");
        maogan_login_token = sharedPreferences.getString("maogan_login_token", "0");
        refreshLoginStatus();

    }

    private void refreshDataList() {
        String localFilePath = PathUtils.getExternalAppFilesPath() + File.separator + "MaoGanData";
        List<File> fileList = FileUtils.listFilesInDir(localFilePath);
        MaoganLoaclDataListAdapater maoganLoaclDataListAdapater = new MaoganLoaclDataListAdapater(this, fileList, iMaoganDataUpdata);
        listdata.setAdapter(maoganLoaclDataListAdapater);
        // listdata.getCheckedItemCount();
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
                    layprogramparamter.setVisibility(View.GONE);
                    binding.layoutPointRecord.setVisibility(View.VISIBLE);
                    lldevice.setVisibility(View.GONE);
                    binding.tvPointRecord.setVisibility(View.VISIBLE);
                    binding.tvProgramParamter.setVisibility(View.VISIBLE);
                    binding.tvDataDownload.setText("连接设备");
                }
                break;
            case R.id.btn_login: {
                httpPostrequest(Url_Get_Setting);
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
                        if (bleDeviceName.contains("YHZ") || bleDeviceName.contains("HLK") || bleDeviceName.contains("Maogan")) {

                            lvDevicesAdapter.addDevice(bleDevice);
                            if (binding.tvNotDevice.getVisibility() == View.VISIBLE) {
                                binding.tvNotDevice.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;

                case SELECT_DEVICE:
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
                    binding.layoutPointRecord.setVisibility(View.VISIBLE);
                    // binding.layProgramParamter.setVisibility(View.VISIBLE);
                    binding.tvPointRecord.setVisibility(View.VISIBLE);
                    binding.tvProgramParamter.setVisibility(View.VISIBLE);
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
                    //long t1 = System.currentTimeMillis();
                    receiveMessage(recBufSuc);
                    //long t2 = System.currentTimeMillis();
                  //  Log.w(TAG, String.valueOf(t2 - t1));

                    //  String receiveResult = TypeConversion.bytes2HexString(recBufSuc, recBufSuc.length);
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

    private int status = 0;//
    private int tmpPosition = 0;
    private void receiveMessage(byte[] recBufSuc, boolean isReceiveDataPackeage) {
        if ((recBufSuc[0] & 0xFF) == 0xeb && (recBufSuc[1] & 0xFF) == 0x90 &&  (recBufSuc[2] & 0xFF) == 0x80 &&  (recBufSuc[3] & 0xFF) == 0x7F) {
            status = 1;
            tmpPosition=0;
            dataFramePosition = 0;
            dataPackeageCount =0;
            bleManager.sendMessage("55");
        }
        switch (status){
            case 1:{
                if ((recBufSuc[0] & 0xFF) == 0xeb && (recBufSuc[1] & 0xFF) == 0x90 &&  (recBufSuc[2] & 0xFF) == 0x01 &&  (recBufSuc[3] & 0xFF) == 0xFE) {
                    status = 2;
                    System.arraycopy(recBufSuc, 6, cacheBuff, dataFramePosition, 64);
                    dataFramePosition += 64;
                    bleManager.sendMessage("55");
                }
                break;
            }
            case 2:{
                if ((recBufSuc[0] & 0xFF) == 0xeb && (recBufSuc[1] & 0xFF) == 0x90 &&  (recBufSuc[4] & 0xFF) == 0x00 &&  (recBufSuc[5] & 0xFF) == 0x02) {
                    status = 3;
                    System.arraycopy(recBufSuc, 6, cacheBuff, dataFramePosition, recBufSuc.length-6);
                    dataFramePosition += recBufSuc.length-6;
                    tmpPosition +=recBufSuc.length-6;
                }
                break;
            }
            case 3:{
                System.arraycopy(recBufSuc, 0, cacheBuff, dataFramePosition, recBufSuc.length);
                dataFramePosition += recBufSuc.length;
                tmpPosition +=recBufSuc.length;
                if (tmpPosition>=512){
                    status = 4;
                    tmpPosition=0;
                    bleManager.sendMessage("55");
                }
                break;
            }
            case 4:{
                if ((recBufSuc[0] & 0xFF) == 0xeb && (recBufSuc[1] & 0xFF) == 0x90 &&  (recBufSuc[4] & 0xFF) == 0x00 &&  (recBufSuc[5] & 0xFF) == 0x10) {
                    status = 5;
                    System.arraycopy(recBufSuc, 6, cacheBuff, dataFramePosition, recBufSuc.length-6);
                    dataFramePosition += recBufSuc.length-6;
                    tmpPosition +=recBufSuc.length-6;
                }
                break;
            }
            case 5:{
                System.arraycopy(recBufSuc, 0, cacheBuff, dataFramePosition, recBufSuc.length);
                dataFramePosition += recBufSuc.length;
                tmpPosition +=recBufSuc.length;
                if (tmpPosition==4098){
                    status = 4;
                    tmpPosition=0;
                    dataPackeageCount ++;
                    if (dataPackeageCount==6){
                        dataPackeageCount=0;
                        ToastUtils.showLong("传输成功！");
                    }

                    bleManager.sendMessage("55");
                }
                break;
            }

        }

    }

    private byte [] cacheBuff = new byte[1024*40];

    private void receiveMessage(byte[] recBufSuc) {

        if ((recBufSuc[0] & 0xFF) == 0xeb && (recBufSuc[1] & 0xFF) == 0x90 && ((recBufSuc[2] & 0xFF) + (recBufSuc[3] & 0xFF)) == 255) {

            int num1 = Byte.toUnsignedInt(recBufSuc[4]);
            int num2 = Byte.toUnsignedInt(recBufSuc[5]) * 256;
            int length = num1 + num2;
            if (isReceiveDataPackeage) return;
            if (length == 0) {
                bleManager.sendMessage("55");
                return;
            } else {
                if (length > BLE_Maxsize_Packeage) {
                    dataPackeagelength = length;
                    try {
                        int start = 6;
                        buf = new byte[length];
                        System.arraycopy(recBufSuc, start, buf, 0, 199);
                        dataFramePosition += 199;
                        isReceiveDataPackeage = true;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    try {
                        int start = 6;
                        byte[] FileHeadBuf = new byte[length];
                        System.arraycopy(recBufSuc, start, FileHeadBuf, 0, length);
                        parseFileInfo(FileHeadBuf);
                        bleManager.sendMessage("55");
                    } catch
                    (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        } else {
            try {
                if (isReceiveDataPackeage) {
                    if (dataFramePosition + recBufSuc.length - 2 >= dataPackeagelength) {
                        System.arraycopy(recBufSuc, 0, buf, dataFramePosition, recBufSuc.length - 2);
                        dataFramePosition += (recBufSuc.length + 2);
                        int error = 1;

                        if (dataPackeagelength == 512) {
                            try {
                                fos.write(buf);
                                fos.flush();
                            } catch (Exception ex) {
                                ToastUtils.showLong(ex.getMessage());
                            }
                        } else if (dataPackeagelength == 4096) {
                            dataPackeageCount++;
                            try {
                                fos.write(buf);
                                fos.flush();
                            } catch (Exception ex) {
                                ToastUtils.showLong(ex.getMessage());
                            }
                            if (dataPackeageCount == 6) {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                        ToastUtils.showLong("传输成功！");
                                        dataPackeageCount = 0;
                                    } catch (Exception ex) {
                                        ToastUtils.showLong(ex.getMessage());
                                    }
                                }
                            }
                        }
                        dataFramePosition = 0;
                        checkSum = 0;
                        isReceiveDataPackeage = false;
                        bleManager.sendMessage("55");


                    } else {
                        System.arraycopy(recBufSuc, 0, buf, dataFramePosition, recBufSuc.length);
                        dataFramePosition += recBufSuc.length;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    FileOutputStream fos = null;

    public class MaoganFileInfo {
        String fileName;
        int fileSize;
        int fileHeadSize;
        int fileSegSize;
        int fileSegNum;
    }

    private class MaoganData {
        public MaoganFileHead maoganFileHead;
        public MaoganFileInfo maoganFileInfo;
        public List<MaoganFileseg> maoganFilesegList;
    }

    private void parseFileInfo(byte[] buff) {
        int start = 0;
        MaoganFileInfo maoganFileInfo = new MaoganFileInfo();
        byte[] tmpBuff2 = new byte[48];
        System.arraycopy(buff, start, tmpBuff2, 0, 48);
        maoganFileInfo.fileName = new String(tmpBuff2, StandardCharsets.UTF_8);
        start += 48;
        saveDataFile(maoganFileInfo.fileName, buff);
    }

    private void saveDataFile(String fileName, byte[] buff) {
        String localFilePath = PathUtils.getExternalAppFilesPath() + File.separator + "MaoGanData";
        if (FileUtils.isFileExists(localFilePath)) {

        } else {
            FileUtils.createOrExistsDir(localFilePath);
        }
        try {
            fileName = fileName.trim();
            List<File> fileList = FileUtils.listFilesInDir(localFilePath);
            String dataPath = localFilePath + File.separator + fileName + ".dat";
            if (FileUtils.isFileExists(dataPath)) {
                if (fos != null) {
                    fos.close();
                    if (FileUtils.delete(dataPath)) {
                        ToastUtils.showLong("删除成功");
                    } else {
                        ToastUtils.showLong("删除失败");
                    }
                }
            }
            fos = new FileOutputStream(dataPath);
            fos.write(buff);
            fos.flush();
        } catch (Exception ex) {
            ToastUtils.showLong(ex.getMessage());
        }

    }

    public class MaoganFileHead {
        int fileSysId;
        int fileSysVer;
        int SampleNum;
        String prj_name;
        String serial_num;
        String peg_pos;
        String test_department;
        String test_person;
        String test_data;
        String peg_type;
        int waveSpeed;
        int peg_length;
        int samp_interval;
        int scale_mode;
        int[] scale_seg;
        int scale_endpoint;
        int sampleRate;

        int fileVer;
        int samp_mode;
        int coordinate_unit;
        int trig_mode;
        int trig_level;
        int ruler_mode;
        int sig_pole;
        int lp_freg;
        int hp_freq;
    }

    private MaoganFileHead parseFileHead(byte[] buff) {
        MaoganFileHead maoganFileHead = new MaoganFileHead();
        if (buff.length == 512) {
            int start = 0;
            byte[] tmpBuff = new byte[4];
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.fileSysId = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.fileSysVer = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.SampleNum = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;

            /////256字节
            byte[] tmpBuff2 = new byte[24];
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            char[] tmpChar = ConvertUtils.bytes2Chars(tmpBuff2);

            maoganFileHead.prj_name = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            maoganFileHead.serial_num = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            maoganFileHead.peg_pos = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            maoganFileHead.test_department = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            maoganFileHead.test_person = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            maoganFileHead.test_data = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff2, 0, 24);
            maoganFileHead.peg_type = new String(tmpBuff2, StandardCharsets.UTF_8);
            start += 24;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.waveSpeed = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.peg_length = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.samp_interval = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.scale_mode = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            int[] scale_seg = new int[11];
            for (int i = 0; i < 11; i++) {
                System.arraycopy(buff, start, tmpBuff, 0, 4);
                scale_seg[i] = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
                start += 4;
            }
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.scale_endpoint = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.sampleRate = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            start += 20;
            ///////256字节

            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.fileVer = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;

            /////128字节
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.samp_mode = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.coordinate_unit = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.trig_mode = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.trig_level = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.ruler_mode = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.sig_pole = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.lp_freg = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;
            System.arraycopy(buff, start, tmpBuff, 0, 4);
            maoganFileHead.hp_freq = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFF;
            start += 4;

        } else {
            ToastUtils.showLong("文件头长度错误，为" + buff.length);
        }
        return maoganFileHead;
    }

    private List<MaoganFileseg> maoganFilesegList = new ArrayList<>();

    public class MaoganFileseg {
        int max;
        int max_x;
        int bConfirm;
        int sampRate;
        int length;
        int[] cursor;
        int[] data;
    }

    private void parseFileSeg(byte[] buf) {
        if (buf.length == 4096) {
            MaoganFileseg maoganFileseg = new MaoganFileseg();
            int start = 0;
            byte[] tmpBuff = new byte[4];
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.max = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.max_x = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.bConfirm = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.sampRate = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            maoganFileseg.cursor = new int[2];
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.cursor[0] = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.cursor[1] = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            System.arraycopy(buf, start, tmpBuff, 0, 4);
            maoganFileseg.length = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
            start += 4;
            maoganFileseg.data = new int[1017];
            for (int i = 0; i < 1017; i++) {
                System.arraycopy(buf, start, tmpBuff, 0, 4);
                maoganFileseg.data[i] = ByteBuffer.wrap(tmpBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
                start += 4;
            }
            maoganFilesegList.add(maoganFileseg);
            try {
                fos.write(buf);
                fos.flush();
            } catch (Exception ex) {
                ToastUtils.showLong(ex.getMessage());
            }
        } else {
            ToastUtils.showLong("数据片段长度错误,长度为:" + buf.length);
        }
    }

    private void initBLE() {
        //列表适配器
        lvDevicesAdapter = new LVDevicesAdapter(MaoganMainActivity.this);
        lvDevices.setAdapter(lvDevicesAdapter);
        bleManager = new BLEManager();
        if (!bleManager.initBle(mContext)) {//EB 90 80 7F 00 00
            Log.d(TAG, "该设备不支持低功耗蓝牙");
            Toast.makeText(mContext, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    private void initListener() {
        binding.tvDataDownload.setOnClickListener(this);
        binding.tvPointRecord.setOnClickListener(this);
        binding.tvProgramParamter.setOnClickListener(this);
        binding.startButton.setOnClickListener(this);
        binding.ivBack.setOnClickListener(this);
        binding.btnLogin.setOnClickListener(this);
        lvDevices.setOnItemClickListener((adapterView, view, i, l) -> {
            BLEDevice bleDevice = (BLEDevice) lvDevicesAdapter.getItem(i);
            BluetoothDevice bluetoothDevice = bleDevice.getBluetoothDevice();
            if (bleManager != null) {
                bleManager.stopDiscoveryDevice();
            }
            Message message = new Message();
            message.what = SELECT_DEVICE;
            message.obj = bluetoothDevice;
            mHandler.sendMessage(message);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            bluetoothLeScanner.startScan(null, settings, scanCallback);
        } else {
            bleManager.startDiscoveryDevice(onDeviceSearchListener, 15000);
        }
        //开始搜索

    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device1 = result.getDevice();
            @SuppressLint("MissingPermission") String bleDeviceName = device1.getName();
            if (bleDeviceName != null) {
                if (bleDeviceName.contains("YHZ") || bleDeviceName.contains("HLK") || bleDeviceName.contains("Maogan")) {

                    lvDevicesAdapter.addDevice(new BLEDevice(device1, 100));
                    if (binding.tvNotDevice.getVisibility() == View.VISIBLE) {
                        binding.tvNotDevice.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

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
            if (binding.tvNotDevice.getVisibility() == View.VISIBLE) {
                binding.ivStep1.setState(3);
                binding.tvStep1Text.setText("扫描设备超时");
            } else if (binding.tvNotDevice.getVisibility() == View.GONE) {
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        try {
//            bleManager.disConnectDevice();
//        } catch (Exception ex) {
//            ToastUtils.showLong(ex.getLocalizedMessage());
//        }
//    }


}