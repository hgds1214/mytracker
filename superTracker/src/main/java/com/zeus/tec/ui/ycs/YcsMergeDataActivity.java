package com.zeus.tec.ui.ycs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityYcsMergeDataBinding;
import com.zeus.tec.model.utils.FeedbackUtil;
import com.zeus.tec.model.ycs.YcsMainCache;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class YcsMergeDataActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityYcsMergeDataBinding binding;

    YcsMainCache ycsCache = YcsMainCache.GetInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYcsMergeDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initListener();
        init();
    }

    private void initView() {
      //  View decorView = getWindow().getDecorView();
       // decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        BarUtils.setStatusBarVisibility( this,false);
        initLineChart(binding.responseCurveChart);
        initMultiChannelChart(binding.multiChannelCurveChart);
    }

    private void  initListener (){
        binding.xOrientationBtn.setOnClickListener(this);
        binding.yOrientationBtn.setOnClickListener(this);
        binding.zOrientationBt.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
    }

    private void init() {
        readYcsData(ycsCache.ycsDataFileInfo.filePath + File.separator + ycsCache.ycsDataFileInfo.x_ycs_file,Color.RED);
    }

    List<float[]> dataList = new ArrayList<>();


    private void readYcsData(String ycsDataPath,int LineColor) {
        List<String> ycsDataStrs = FileIOUtils.readFile2List(ycsDataPath);
        dataList.clear();
        String[] tmpStrs = ycsDataStrs.get(0).split("\t");
        int [] timelist = new int[tmpStrs.length-22];
        for (int i = 22; i < tmpStrs.length; i++) {
            timelist[i-22] = Integer.parseInt(tmpStrs[i] );
        }
        String[] DataStrs;
        float[] voltData;
        for (int i = 1; i < ycsDataStrs.size(); i++) {
            DataStrs = ycsDataStrs.get(i).split("\t");
            voltData = new float[DataStrs.length - 22];
            for (int j = 22; j < DataStrs.length; j++) {
                voltData[j - 22] = Float.parseFloat(DataStrs[j]);
            }
            dataList.add(voltData);
        }
        drawResponseCurve(dataList,timelist,LineColor);
        drawMultiChannelCurve(dataList,LineColor);
    }

    private void initLineChart(LineChart chart) {
        Description description = new Description();
        description.setText("响应曲线 Y轴:感应电动势Log(V)|(uV/A) X轴:时间|(ms)");
        description.setTextColor(Color.BLACK);
        description.setYOffset(15);
        description.setXOffset(3);
        chart.setDescription(description);
        chart.setNoDataText("没有数据可以显示");//设置当没有数据时的提示文本
        chart.setExtraBottomOffset(5);//额外的底部间距
        chart.setMinOffset(2);

        XAxis xAxis = chart.getXAxis();
//        xAxis.setAxisMaximum(4.5f);
//        xAxis.setAxisMinimum(0);
//        xAxis.setGranularity(1f); // 每隔 10 显示一个标签
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int num1 = (int) value;
                double num2;
                if (num1 == 0) {
                    num2 = 0;
                } else {
                    num2 = 0.001;
                }
                for (int i = 0; i < num1; i++) {
                    num2 = num2 * 10;
                }
                return String.valueOf(num2);
            }
        });
        chart.setDrawBorders(true);
//        chart.setMinOffset(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setEnabled(true);
//        xAxis.setTextColor(Color.BLACK);
//        xAxis.setDrawGridLinesBehindData(true);//当设置为 true：网格线在数据图形的背后绘制。
        xAxis.setAvoidFirstLastClipping(true);//当设置为 true：X 轴的第一个和最后一个标签将会自动留出一定的空白间距
//        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5f, 5f}, 1);
//        xAxis.setGridDashedLine(dashPathEffect);
//        xAxis.setAxisLineWidth(2);

//        xAxis.setLabelCount(8);
//        YAxis leftYAxis = chart.getAxisLeft();
//
//        leftYAxis.setEnabled(true);
//        leftYAxis.setDrawGridLines(true);
//        leftYAxis.setGridDashedLine(dashPathEffect);
//        leftYAxis.setDrawGridLinesBehindData(true);
//        leftYAxis.setGridLineWidth(1);
//        leftYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        leftYAxis.setTextColor(Color.BLACK);
//        leftYAxis.setDrawZeroLine(true);
//        leftYAxis.setYOffset(5);
        //   rightYaxis.setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
        // yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        yAxis.setAxisMaximum(6f);
//        yAxis.setYOffset(-5);
//        yAxis.setAxisMinimum(-4f);
//        yAxis.setGranularity(1f); // 每隔 10 显示一个标签
//        yAxis.setLabelCount(9, false);
//        yAxis.setGranularityEnabled(true);
//        yAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return String.format("10^%.0f", value); //将 log10 值转换为指数形式
//            }
//        });
        YAxis rightYaxis = chart.getAxisRight();
        rightYaxis.setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setDrawInside(false);
        legend.setEnabled(false);
    }

    private void initMultiChannelChart(LineChart chart) {
        Description description = new Description();
        description.setText("多测道曲线 Y轴:感应电动势Log(V)|(uV/A) X轴:距离|(M)");
        description.setTextColor(Color.BLACK);
        description.setYOffset(15);
        description.setXOffset(15);
        chart.setDescription(description);
        chart.setExtraRightOffset(0);//300
        chart.setMinOffset(2);//设置同周围图表的间距
        chart.setExtraBottomOffset(5);//额外的底部间距
       // chart.setExtraTopOffset(5);//额外的底部间距
        XAxis xAxis = chart.getXAxis();
//        xAxis.setAxisMaximum(4.5f);
//        xAxis.setAxisMinimum(0);
//        xAxis.setGranularity(1f); // 每隔 10 显示一个标签
//        xAxis.setGranularityEnabled(true);
//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                int num1 = (int) value;
//                double num2;
//                if (num1 == 0) {
//                    num2 = 0;
//                } else {
//                    num2 = 0.001;
//                }
//                for (int i = 0; i < num1; i++) {
//                    num2 = num2 * 10;
//                }
//                return String.valueOf(num2);
//            }
//        });
        chart.setDrawBorders(true);
//        chart.setMinOffset(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setEnabled(true);
//        xAxis.setTextColor(Color.BLACK);
//        xAxis.setDrawGridLinesBehindData(true);//当设置为 true：网格线在数据图形的背后绘制。
        xAxis.setAvoidFirstLastClipping(true);
        //当设置为 true：X 轴的第一个和最后一个标签将会自动留出一定的空白间距
//        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5f, 5f}, 1);
//        xAxis.setGridDashedLine(dashPathEffect);
//        xAxis.setAxisLineWidth(2);

//        xAxis.setLabelCount(8);
//        YAxis leftYAxis = chart.getAxisLeft();
//
//        leftYAxis.setEnabled(true);
//        leftYAxis.setDrawGridLines(true);
//        leftYAxis.setGridDashedLine(dashPathEffect);
//        leftYAxis.setDrawGridLinesBehindData(true);
//        leftYAxis.setGridLineWidth(1);
//        leftYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        leftYAxis.setTextColor(Color.BLACK);
//        leftYAxis.setDrawZeroLine(true);
//        leftYAxis.setYOffset(5);
        //   rightYaxis.setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
        // yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        yAxis.setAxisMaximum(6f);
//        yAxis.setYOffset(-5);
//        yAxis.setAxisMinimum(-4f);
//        yAxis.setGranularity(1f); // 每隔 10 显示一个标签
//        yAxis.setLabelCount(9, false);
//        yAxis.setGranularityEnabled(true);
//        yAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return String.format("10^%.0f", value); //将 log10 值转换为指数形式
//            }
//        });
        YAxis rightYaxis = chart.getAxisRight();
        rightYaxis.setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setDrawInside(false);
        legend.setEnabled(false);
    }

    private void drawResponseCurve(List<float[]> dataList, int[] timeList,int LineColor) {
        LineChart chart = binding.responseCurveChart;
        chart.setVisibility(View.VISIBLE);
        LineData lineData = new LineData();
        LineDataSet dataSet;
        List<Entry> entries;

        if (false){
            for (int i = 0; i < dataList.size(); i++) {
                entries = new ArrayList<>();
                int size = dataList.get(i).length;
                for (int j = 0; j < size; j++) {

                    if (Math.log10(dataList.get(i)[j]) > 0) {
                        entries.add(new Entry((float) Math.log10(timeList[j]), (float) (Math.log10(dataList.get(i)[j]))));
                    } else {
                        entries.add(new Entry((float) Math.log10(timeList[j]), (float) (Math.log10(Math.abs(dataList.get(i)[j])))));
                    }
                }
                dataSet = new LineDataSet(entries,"");
                dataSet.setDrawCircles(false);
                dataSet.setColor(LineColor);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setLineWidth(1);
                dataSet.setDrawValues(false);
                lineData.addDataSet(dataSet);
                chart.setData(lineData);
            }
        }else {
            for (int i = 0; i < dataList.size(); i++) {
                entries = new ArrayList<>();
                int size = dataList.get(i).length;
                for (int j = 23; j < size; j++) {
                    entries.add(new Entry((float) Math.log10(timeList[j]), (float) ((dataList.get(i)[j]))));
//                    if (Math.log10(dataList.get(i)[j]) > 0) {
//                        entries.add(new Entry((float) Math.log10(timeList[j]), (float) ((dataList.get(i)[j]))));
//                    } else {
//                        entries.add(new Entry((float) Math.log10(timeList[j]), (float) ((Math.abs(dataList.get(i)[j])))));
//                    }
                }
                dataSet = new LineDataSet(entries,"");
                dataSet.setDrawCircles(false);
                dataSet.setColor(LineColor);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                dataSet.setLineWidth(1);
                dataSet.setDrawValues(false);
                lineData.addDataSet(dataSet);
                chart.setData(lineData);
            }
        }
        chart.invalidate();
    }

    private void drawMultiChannelCurve (List<float[]> dataList,int lineColor){
        LineChart chart = binding.multiChannelCurveChart;
        chart.setVisibility(View.VISIBLE);
        LineData lineData = new LineData();
        LineDataSet dataSet;
        List<Entry> entries;
        int size = dataList.get(0).length;
        if (false){
            for (int i = 0; i < size; i++) {
                entries = new ArrayList<>();
                for (int j = 0; j < dataList.size(); j++) {
                    if (Math.log10(dataList.get(j)[i])>0){
                        entries.add(new Entry(j,(float) Math.log10(dataList.get(j)[i])));
                    }else {
                        entries.add(new Entry(j,(float) Math.log10(Math.abs(dataList.get(j)[i]))));
                    }
                }
                dataSet = new LineDataSet(entries,"");
                dataSet.setDrawCircles(false);
                dataSet.setColor(lineColor);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                dataSet.setLineWidth(1);
                dataSet.setDrawValues(false);
                lineData.addDataSet(dataSet);
                chart.setData(lineData);
            }
        }
        else {
            for (int i = 23; i < size; i++) {
                entries = new ArrayList<>();
                for (int j = 0; j < dataList.size(); j++) {
                    entries.add(new Entry(j,dataList.get(j)[i]));
//                    if (Math.log10(dataList.get(j)[i])>0){
//                        entries.add(new Entry(j,(float) Math.log10(dataList.get(j)[i])));
//                    }else {
//                        entries.add(new Entry(j,(float) Math.log10(Math.abs(dataList.get(j)[i]))));
//                    }
                }
                dataSet = new LineDataSet(entries,"");
                dataSet.setDrawCircles(false);
                dataSet.setColor(lineColor);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                dataSet.setLineWidth(1);
                dataSet.setDrawValues(false);
                lineData.addDataSet(dataSet);
                chart.setData(lineData);
            }
        }

        chart.invalidate();

    }


    @Override
    public void onClick(View v) {
        FeedbackUtil.getInstance().doFeedback();
        switch (v.getId()){
            case R.id.x_orientation_btn:{
                readYcsData(ycsCache.ycsDataFileInfo.filePath + File.separator + ycsCache.ycsDataFileInfo.x_ycs_file,Color.RED);
                break;
            }
            case R.id.y_orientation_btn:{
                readYcsData(ycsCache.ycsDataFileInfo.filePath + File.separator + ycsCache.ycsDataFileInfo.y_ycs_file,Color.argb(255,57,125,84));
                break;
            }
            case R.id.z_orientation_bt:{
                readYcsData(ycsCache.ycsDataFileInfo.filePath + File.separator + ycsCache.ycsDataFileInfo.z_ycs_file,Color.BLUE);
                break;
            }
            case R.id.back_btn:{
                finish();
                break;
            }
        }
    }
}