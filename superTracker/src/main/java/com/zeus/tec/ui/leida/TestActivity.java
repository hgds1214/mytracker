package com.zeus.tec.ui.leida;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ToastUtils;
import com.zeus.tec.R;
import com.zeus.tec.model.leida.main.MainCache;
import com.zeus.tec.model.leida.sampleTest.DataCache;
import com.zeus.tec.model.leida.sampleTest.DrawSampleThread;
import com.zeus.tec.model.leida.sampleTest.Message1;
import com.zeus.tec.ui.leida.util.DecimalFormat1;
import com.zeus.tec.ui.leida.util.fastToast;
import com.zeus.tec.ui.tracker.util.TextHelper;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.IOException;

public class TestActivity extends Fragment {

    private String[] starArray = {"512", "1024", "2048"};
    private String[] frequencyArray = {"1000", "2000", "4000"};
    int sampleLength_index = 1;
    int frequency_index = 1;
    TextView edtAmp;
    TextView edtOverlaynumber;
    TextView edtDelay;
    TextView timeinterval;
    TextView device_status;
    TextView device_voltage;
    TextView device_gyro ;
    MainCache cache2 = MainCache.GetInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_projectinfo_sampletest, container, false);

        Spinner sp_frequency = view.findViewById(R.id.sp_Frequency);
        Spinner sp_startAdapter = view.findViewById(R.id.sp_sampleLength);
        ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(context, R.layout.item_select, starArray);
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<String>(context, R.layout.item_select, frequencyArray);

        edtAmp = view.findViewById(R.id.edt_Amp);
        edtAmp.setText("1");
        edtOverlaynumber = view.findViewById(R.id.edt_overlaynumber);
        edtOverlaynumber.setText("32");
        edtDelay = view.findViewById(R.id.edt_delay);
        edtDelay.setText("1");
        timeinterval = view.findViewById(R.id.edt_time_space);
        timeinterval.setText("100");
         device_status = view.findViewById(R.id.edt_device_status);
         device_voltage = view.findViewById(R.id.edt_device_voltage);
         device_gyro = view.findViewById(R.id.edt_device_gyro);


        TextView setting_param = view.findViewById(R.id.tv_setting);
        TextView start_test = view.findViewById(R.id.tv_start);
        TextView stop_test = view.findViewById(R.id.tv_stop);
        setting_param.setOnClickListener(v -> {
            try {
                setting_param_click();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        start_test.setOnClickListener(v -> {
            try {
                start_test_click();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        stop_test.setOnClickListener(v -> stop_test_click());


        edtAmp.setText(TextHelper.safeString("" + DataCache.Amp));
        edtDelay.setText(TextHelper.safeString("" + DataCache.DelayPointNumber));
        edtOverlaynumber.setText(TextHelper.safeString("" + DataCache.stackCount));
        timeinterval.setText(TextHelper.safeString("" + DataCache.timeinterval));
        //tvGyRO2.setText(TextHelper.safeString("" + info.GYROThreshold));

        sp_frequency.setAdapter(frequencyAdapter);
        sp_startAdapter.setAdapter(startAdapter);
        sp_frequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                frequency_index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                frequency_index = 0;
            }
        });
        sp_startAdapter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sampleLength_index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sampleLength_index = 0;
            }
        });
        boolean firstShowDeviceStatus = true;
        Thread TgetdeviceStatus = new Thread(() -> {
            try {
                ShowDeviceStatus(device_status, device_voltage, device_gyro);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        TgetdeviceStatus.start();
        return view;
    }

    DataCache cache1 = DataCache.GetInstance();
    DrawSampleThread drawSampleThread ;



    public  void  getDeviceStatus (){
        Thread TgetdeviceStatus = new Thread(() -> {

            try {
                ShowDeviceStatus(device_status, device_voltage, device_gyro);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        TgetdeviceStatus.start();
    }



    public void setting_param_click() throws IOException, InterruptedException {
        FeedbackUtil.getInstance().doFeedback();
        judgment();

    }

    public void start_test_click() throws IOException, InterruptedException {

    }

    public void stop_test_click() {
        FeedbackUtil.getInstance().doFeedback();
        getDeviceStatus();

    }

    public void judgment() throws InterruptedException, IOException {

        String edtAmpstring = edtAmp.getText().toString();
        if (TextUtils.isEmpty(edtAmpstring)) {
            ToastUtils.showLong("放大倍数不能为空");
            return;
        }
        int edtAmp = Integer.parseInt(edtAmpstring);
        String edtDelaystring = edtDelay.getText().toString();
        if (TextUtils.isEmpty(edtDelaystring)) {
            ToastUtils.showLong("延迟点数不能为空");
            return;
        }
        int edtDelay = Integer.parseInt(edtDelaystring);
        //   String edtOverlaynumber = edtOverlaynumber.getText().toString();
        String edtoverlaynumber = edtOverlaynumber.getText().toString();
        if (TextUtils.isEmpty(edtoverlaynumber)) {
            ToastUtils.showLong("叠加次数不能为空");
            return;
        }
        int overLayNumber = Integer.parseInt(edtoverlaynumber);
        if (overLayNumber < 1 || overLayNumber > 1000) {
            ToastUtils.showLong("叠加次数在" + 1000 + "以内");
            return;
        }
        String edtTimeSpace = timeinterval.getText().toString();
        if (TextUtils.isEmpty(edtTimeSpace)) {
            ToastUtils.showLong("时间间隔(ms)");
            return;
        }
        float timeSpace = Float.parseFloat(edtTimeSpace);
        if (timeSpace < 100 || timeSpace > 1000) {
            ToastUtils.showLong("时间间隔只能是100到" + 1000 + "之间");
            return;
        }
        float GYROThreshold = 50;
        int AmplifyValue = GetAmp(edtAmp);
        int SampleCount = sampleLength_index;
        int SampleFrequency = frequency_index;

        if (cache1.send.code == 0x00) {
            cache1.send.code = 0x01;
        } else {
            cache1.send.code = 0x00;
        }

        Message1 message = new Message1();
        byte[] data = message.SettingOrder(cache1.send.code, Integer.parseInt(starArray[SampleCount]), overLayNumber, edtDelay, AmplifyValue, Integer.parseInt(frequencyArray[SampleFrequency]), timeSpace, GYROThreshold);
        cache1.SettingStatus = new DataCache.SettingStatusBean();
        cache1.SettingStatus.SettingStatus = -1;
        Thread th = new Thread(() -> {
            try {
                cache1.send.Send(data);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        th.start();

        Thread.sleep(2000);
        if (cache1.SettingStatus != null) {
            if (cache1.SettingStatus.SettingStatus == 1) {
                cache1.sampleintervel = timeSpace;
               // cache2.test_setting = new TestSetting();
                cache2.test_setting.Amplify = edtAmp;
                cache2.test_setting.TimeInterval = timeSpace;
                cache2.test_setting.SampleCount = SampleCount;
                cache2.test_setting.StackCount = overLayNumber;
                cache2.test_setting.NbOfSampleDelayPoint = edtDelay;
                cache2.test_setting.SampleFrequency = SampleFrequency;
                fastToast.showToast("配置成功");
                // MessageBox.Show("配置成功!");
            } else if (cache1.SettingStatus.SettingStatus == 0) {
                // MessageBox.Show("配置失败请重试!");
                fastToast.showToast("配置失败请重试");
            } else {
                fastToast.showToast("配置命令发送失败1，请重试");
                //  MessageBox.Show("配置命令发送失败，请重试!");
            }
        } else {
            fastToast.showToast("配置命令发送失败2，请重试！");
            // MessageBox.Show("配置命令发送失败，请重试!");
        }
        cache1.SettingStatus = null;
    }

    private int GetAmp(float amp) {
        int result = 0;
        result = (int) (amp * 2.0);
        return result;
    }

    public void ShowDeviceStatus(TextView device_status, TextView device_voltage, TextView device_gyro) throws IOException, InterruptedException {

        // TimeSpan ts = DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, 0);
        try {
            System.currentTimeMillis();
            long current_time = System.currentTimeMillis();
            if (current_time - cache1.LastTime > 5000) {
                Message1 message = new Message1();
                byte[] data = message.GetDeviceStatus(cache1.send.code);
                cache1.DeviceStatus = new DataCache.StatusBean();
                cache1.DeviceStatus.Time = 0;
                cache1.send.Send(data);
                Thread.sleep(1000);
                if (cache1.DeviceStatus != null) {
                    long LastTime = cache1.DeviceStatus.Time;
                    int workmode = cache1.DeviceStatus.WorkStatus;
                    float electric = cache1.DeviceStatus.Electricity;
                    float gyro = cache1.DeviceStatus.GYRO;
                    cache1.LastTime = LastTime;
                    if ((current_time - cache1.LastTime) > 5000) {
                        ((Activity) context).runOnUiThread(() -> device_status.setText("未连接"));
                    } else {
                        if (workmode == 0) {
                            ((Activity) context).runOnUiThread(() -> device_status.setText("空闲"));

                        } else if (workmode == 1) {
                            ((Activity) context).runOnUiThread(() -> device_status.setText("定时采样中"));

                        } else if (workmode == 2) {
                            ((Activity) context).runOnUiThread(() -> device_status.setText("触发采样中"));
                        } else {
                            ((Activity) context).runOnUiThread(() -> device_status.setText("未知"));
                        }

                        ((Activity) context).runOnUiThread(() -> {
                            device_voltage.setText(DecimalFormat1.getdecimalFormat(electric, 1));
                            device_gyro.setText(DecimalFormat1.getdecimalFormat(gyro, 2));

                        });
                    }
                } else {
                    ((Activity) context).runOnUiThread(() -> {
                        device_status.setText("未连接");
                        device_voltage.setText("");
                    });
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
    }
}
