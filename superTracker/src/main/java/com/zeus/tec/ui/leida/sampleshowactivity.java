package com.zeus.tec.ui.leida;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ToastUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zeus.tec.R;
import com.zeus.tec.model.leida.sampleTest.DataCache;
import com.zeus.tec.model.leida.sampleTest.DrawSampleThread;
import com.zeus.tec.model.leida.sampleTest.ILeidaReciveListener;
import com.zeus.tec.model.leida.sampleTest.Message1;
import com.zeus.tec.ui.leida.util.ConvertCode;
import com.zeus.tec.ui.leida.util.IOtool;
import com.zeus.tec.ui.leida.util.fastToast;
import com.zeus.tec.model.utils.FeedbackUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class sampleshowactivity extends Fragment {


    DataCache cache1 = DataCache.GetInstance();
    LineChart lineChart;

    LineChart oneSampleChart;
    int SampleSize = 0;

    public ILeidaReciveListener leidaReciveListener = new ILeidaReciveListener() {
        @Override
        public void onReciveData() {

            drawOneSampleChart(cache1.SampleQue.get(cache1.SampleQue.size() - 1));
            drawSampleChart(cache1.SampleQue);
        }

        @Override
        public void onSetStatusCallBack() {
            ToastUtils.showLong("设备启动成功");
        }

        @Override
        public void ongetStatus() {
            if (cache1.DeviceStatus != null) {
                if (cache1.DeviceStatus.WorkStatus == 0) {
                    ToastUtils.showLong("停止采集成功");
                    // CloseReceiveThread();
                } else if (cache1.DeviceStatus.WorkStatus > 0) {
                    //MessageBox1.Show("停止采集失败!");
                    ToastUtils.showLong("停止采集失败");
                } else {
                    // MessageBox.Show("停止采集命令发送失败!");
                    ToastUtils.showLong("停止采集命令失败");
                }
            } else {
                // MessageBox.Show("停止采集命令发送失败!");
                ToastUtils.showLong("停止采集命令发送失败");
            }
            cache1.SettingStatus = null;
            cache1.sendcode = 0;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sample_show_activity, container, false);
    }

    private ActivityResultLauncher<Intent> filePickerLauncher;

    public void start_test_click() throws IOException, InterruptedException {
        FeedbackUtil.getInstance().doFeedback();
        Message1 message = new Message1();
        byte[] data2 = message.GetDeviceStatus(cache1.send.code);
        cache1.DeviceStatus = new DataCache.StatusBean();
        cache1.DeviceStatus.WorkStatus = -1;
        Thread th2 = new Thread(() -> {
            try {
                cache1.send.Send(data2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        th2.start();
        Thread.sleep(3000);
        if (cache1.DeviceStatus != null) {
            if (cache1.DeviceStatus.WorkStatus == 0) {
                if (cache1.send.code == 0x00) {
                    cache1.send.code = 0x01;
                } else {
                    cache1.send.code = 0x00;
                }
                message = new Message1();
                byte[] data1 = message.StartWorkOrder(cache1.send.code);
                cache1.DeviceStatus = new DataCache.StatusBean();
                cache1.DeviceStatus.WorkStatus = -1;
                Thread th3 = new Thread(() -> {
                    try {
                        cache1.send.Send(data1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                th3.start();
                Thread.sleep(1000);
                if (cache1.DeviceStatus != null) {
                    if (cache1.DeviceStatus.WorkStatus > 0) {
                        // CreateSampleExcuteThread();
                        fastToast.showToast("启动采集成功");
                    } else if (cache1.DeviceStatus.WorkStatus == 0) {
                        fastToast.showToast("启动采集失败");
                    } else {
                        fastToast.showToast("启动采集命令发送失败");
                    }
                } else {
                    fastToast.showToast("启动采集命令发送失败");
                }
            } else if (cache1.DeviceStatus.WorkStatus == 1) {
                fastToast.showToast("设备正在工作,中无法请求");
            } else if (cache1.DeviceStatus.WorkStatus == -1) {
                fastToast.showToast("启动采集失败");
            }
        } else {
            fastToast.showToast("启动采集命令发送失败");
        }
    }

    private void Stop_Click() throws IOException, InterruptedException {
        FeedbackUtil.getInstance().doFeedback();
        cache1.sendcode = 2;
        if (cache1.send.code == 0x00) {
            cache1.send.code = 0x01;
        } else {
            cache1.send.code = 0x00;
        }
        Message1 message = new Message1();
        byte[] data = message.StopWorkOrder(cache1.send.code);
        cache1.DeviceStatus = new DataCache.StatusBean();
        cache1.DeviceStatus.WorkStatus = -1;
        Thread th2 = new Thread(() -> {
            try {
                cache1.send.Send(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        th2.start();
    }

    private void initLineChart(LineChart chart) {


        XAxis xAxis = chart.getXAxis();
     //   oneSampleChart.animateY(1000);
      //  oneSampleChart.animateX(1000);
        chart.setDrawBorders(false);
        chart.setMinOffset(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setEnabled(true);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGridLineWidth(1);
        xAxis.setDrawGridLinesBehindData(true);
        xAxis.setAvoidFirstLastClipping(false);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[] {5f,5f},1);
        xAxis.setGridDashedLine(dashPathEffect );
        xAxis.setAxisLineWidth(2);




        xAxis.setLabelCount(8);
        YAxis leftYAxis = chart.getAxisLeft();
        YAxis rightYaxis = chart.getAxisRight();
        leftYAxis.setEnabled(true);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridDashedLine(dashPathEffect);
        leftYAxis.setDrawGridLinesBehindData(true);
        leftYAxis.setGridLineWidth(1);
        leftYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftYAxis.setTextColor(Color.WHITE);
        leftYAxis.setDrawZeroLine(true);
        leftYAxis.setYOffset(5);

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setDrawInside(false);
        rightYaxis.setEnabled(false);
        legend.setEnabled(false);
        DataCache.SampleBean oneSample = new DataCache.SampleBean();
        oneSample.Sample = new float[1024];
        oneSample.SampleLength = 1024;
        oneSample.Max = 1024;
        oneSample.Min = 0;
        drawFirstSampleChart( oneSample,chart);
        // leftYAxis.setEnabled(false);

    }

    public  void  drawFirstSampleChart (DataCache.SampleBean lastSample,LineChart chart){

        LineData lineData = new LineData();
        LineDataSet dataSet ;
        List<Entry> entries = new ArrayList<>();
        int size  = lastSample.SampleLength;
        for (int i = 0; i < 400; i++) {
            entries.add(new Entry(i,lastSample.Sample[i]));
        }
        dataSet = new LineDataSet(entries,"current");
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.GREEN);
        dataSet.setLineWidth(1);
        dataSet.setValueTextColor(Color.YELLOW);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineData.addDataSet(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            TextView input_tv = getView().findViewById(R.id.tv_inputData);
            TextView stop_tv = getView().findViewById(R.id.tv_stop_test);
            lineChart = getView().findViewById(R.id.chart2);
            oneSampleChart = getView().findViewById(R.id.chart1);
            Description onedescription = new Description();
            onedescription.setText("Current Waveform");
            onedescription.setTextColor(Color.GREEN);
            onedescription.setYOffset(15);
            Description description = new Description();
            description.setText("Waveform sequences");
            description.setTextColor(Color.YELLOW);
            description.setYOffset(15);
            lineChart.setDescription(description);

            oneSampleChart.setDescription(onedescription);
            initLineChart(lineChart);
            initLineChart(oneSampleChart);

            stop_tv.setOnClickListener(V -> {
                try {
                    Stop_Click();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            input_tv.setOnClickListener(v -> {
                try {
                    start_test_click();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    Context context;

    public void tvInputData_click() {
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.setType("*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        filePickerLauncher.launch(intent);
    }

    public void drawOneSampleChart(DataCache.SampleBean lastSample) {

        LineData lineData = new LineData();
        LineDataSet dataSet;
        List<Entry> entries = new ArrayList<>();
        int size = lastSample.SampleLength;
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(i, lastSample.Sample[i]));
        }
        //oneSampleChart.getXAxis().setAxisMaximum( lastSample.Max*1.2f);
        dataSet = new LineDataSet(entries, "current");
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.GREEN);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setLineWidth(1);
        lineData.addDataSet(dataSet);
        oneSampleChart.setData(lineData);
        oneSampleChart.invalidate();
    }

    public void drawSampleChart(List<DataCache.SampleBean> sampleBeanList) {
        LineData lineData = new LineData();
        LineDataSet[] datasetList;
        int conut = 8;
        if (sampleBeanList.size() > 7) {
            List<Entry>[] sampleList = new List[conut];
            for (int j = 0; j < conut; j++) {
                datasetList = new LineDataSet[conut];
                sampleList[j] = new ArrayList<Entry>();
                int size = sampleBeanList.get(sampleBeanList.size() - j - 1).SampleLength;
                for (int i = 0; i < size; i++) {
                    sampleList[j].add(new Entry(i, sampleBeanList.get(sampleBeanList.size() - j - 1).Sample[i] + (2.3f * j)));
                }
                datasetList[j] = new LineDataSet(sampleList[j], "s" + j);
                datasetList[j].setColor(Color.rgb(0, 95+ 20*j, 0));
                datasetList[j].setDrawCircles(false);
                datasetList[j].setLineWidth(1);
                datasetList[j].setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineData.addDataSet(datasetList[j]);
            }
        } else {
            datasetList = new LineDataSet[sampleBeanList.size()];
            for (int j = 0; j < sampleBeanList.size(); j++) {
                List<Entry>[] sampleList = new List[sampleBeanList.size()];
                sampleList[j] = new ArrayList<Entry>();
                for (int i = 0; i < 512; i++) {
                    sampleList[j].add(new Entry(1 * i, sampleBeanList.get(sampleBeanList.size() - j - 1).Sample[i] + (2.3f * j)));
                }
                datasetList[j] = new LineDataSet(sampleList[j], "s" + j);
                datasetList[j].setColor(Color.rgb(0, 95+ 20*j, 0));
                datasetList[j].setDrawCircles(false);
                datasetList[j].setLineWidth(1);
                datasetList[j].setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineData.addDataSet(datasetList[j]);
            }
        }
        lineChart.setData(lineData);
       // lineChart.setScaleEnabled(false);
        // lineChart.setBackgroundColor(Color.BLACK);


        lineChart.invalidate();
    }


    public DataCache.SampleBean ReadData(InputStream inputStream) {
        DataCache.SampleBean sampleBean = new DataCache.SampleBean();
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bufferHead = new byte[6]; // 缓冲区大小
            byte[] bufferTime = new byte[8];
            byte[] bufferFileName = new byte[32];
            inputStream.read(bufferHead, 0, 6);
            inputStream.read(bufferFileName, 0, 32);
            byte[] tmp = new byte[2];
            inputStream.read(tmp, 0, 2);
            SampleSize = ConvertCode.getint16(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(tmp, 0, 2);
            DataCache.stackCount = ConvertCode.getint16(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(tmp, 0, 2);
            DataCache.DelayPointNumber = ConvertCode.getint16(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(tmp, 0, 2);
            DataCache.Amp = ConvertCode.getint16(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(tmp, 0, 2);
            DataCache.frequency = ConvertCode.getint16(tmp, ByteOrder.LITTLE_ENDIAN);
            tmp = new byte[4];
            inputStream.read(tmp, 0, 4);
            DataCache.timeinterval = ConvertCode.getFloat(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(bufferTime, 0, 8);
            inputStream.read(tmp, 0, 4);
            sampleBean.dip = ConvertCode.getFloat(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(tmp, 0, 4);
            sampleBean.pich = ConvertCode.getFloat(tmp, ByteOrder.LITTLE_ENDIAN);
            inputStream.read(tmp, 0, 4);
            sampleBean.roll = ConvertCode.getFloat(tmp, ByteOrder.LITTLE_ENDIAN);
            int a = 0;
            while (a == 0) {
                byte[] bufferData = new byte[4];
                sampleBean.SampleLength = SampleSize;
                sampleBean.Sample = new float[sampleBean.SampleLength];
                for (int i = 0; i < SampleSize; i++) {
                    inputStream.read(bufferData, 0, 4);
                    sampleBean.Sample[i] = ConvertCode.getFloat(bufferData, ByteOrder.LITTLE_ENDIAN);
                }
                a = 1;
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sampleBean;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();

        try {
            filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedFileUri = result.getData().getData();
                            // 处理所选文件的 URI
                            // Toast.makeText(this, selectedFileUri.getPath(), Toast.LENGTH_SHORT).show();
                            try {
                                InputStream inputStream = IOtool.openInputStream(context, selectedFileUri);
                                DataCache.SampleBean sampleBean = ReadData(inputStream);
                                // getEntry(sampleBean);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    DrawSampleThread drawSampleThread;


    public void CreateSampleExcuteThread() {

        if (drawSampleThread != null) {

        } else {
            drawSampleThread = new DrawSampleThread(lineChart, context);
            Thread th4 = new Thread(() -> {
                drawSampleThread.Run();
            });
            th4.start();
        }

    }
}
