package com.zeus.tec.ui.leida;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.autofill.AutofillValue;

import com.blankj.utilcode.util.BarUtils;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.zeus.tec.R;
import com.zeus.tec.databinding.ActivityMiraSampleBinding;
import com.zeus.tec.model.leida.sampleTest.DataCache;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MiraSampleActivity extends AppCompatActivity {

    private ActivityMiraSampleBinding binding;
    private LineChart oneSampleChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   setContentView(R.layout.activity_mira_sample);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityMiraSampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(attributes);
        }
        transparentNavBar(this);


        oneSampleChart = binding.chartSample;
        DataCache.SampleBean oneSample = new DataCache.SampleBean();
        oneSample.Sample = new float[1024];
        BarUtils.transparentStatusBar(this);
        Random random = new Random();

        int index = 0;

        for (int i = 0; i < 1024; i++) {
            //  oneSample.Sample[i] = random.nextInt(100);
            //  double x =Math.abs( (double)(i)/100);
            //  oneSample.Sample[index]  = (float)(Math.pow(x,2.0/3.0)+Math.E/3.0*Math.pow(Math.PI-Math.pow(x,2.0),0.5)*Math.sin(19.1*Math.PI*x));
            if (i > 256 && i < 356) {
                oneSample.Sample[i] = (float) Math.sin(i) * Math.abs(306 - i);
            } else {
                oneSample.Sample[i] = (float) Math.sin(i);
            }
            index++;
        }

        oneSample.SampleLength = 1024;
        oneSample.Max = 1024;
        oneSample.Min = 0;
        initchart();
        drawOneSampleChart(oneSample);
        Description onedescription = new Description();
        onedescription.setText("Current Waveform");
        onedescription.setTextColor(Color.YELLOW);

        oneSampleChart.setDescription(onedescription);


        // oneSampleChart.setRotation(90);
        //oneSampleChart.setFitsSystemWindows(true);
    }

    public void initchart() {
        XAxis xAxis = oneSampleChart.getXAxis();

        oneSampleChart.animateY(1000);
        oneSampleChart.animateX(1000);
        oneSampleChart.setDrawBorders(true);
        oneSampleChart.setMinOffset(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setEnabled(true);
        xAxis.setTextColor(Color.WHITE);

        xAxis.setGridLineWidth(1);
        xAxis.setDrawGridLinesBehindData(true);
        xAxis.setAvoidFirstLastClipping(true);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{20f, 15f, 10f, 5f}, 0);

        xAxis.setGridDashedLine(dashPathEffect);
        xAxis.setAxisLineWidth(1);
        xAxis.setLabelCount(8);


        YAxis leftYAxis = oneSampleChart.getAxisLeft();
        YAxis rightYaxis = oneSampleChart.getAxisRight();
        leftYAxis.setEnabled(true);
        leftYAxis.setDrawGridLines(true);
        // leftYAxis.setGridColor(Color.WHITE);
        leftYAxis.setGridDashedLine(dashPathEffect);
        leftYAxis.setGridLineWidth(1);
        leftYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftYAxis.setTextColor(Color.WHITE);

        Legend legend = oneSampleChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setDrawInside(false);

        rightYaxis.setEnabled(false);
        legend.setEnabled(false);
        // leftYAxis.setEnabled(false);

        oneSampleChart.setBackgroundColor(Color.BLACK);
    }

    public static void transparentNavBar(@NonNull final Activity activity) {
        transparentNavBar(activity.getWindow());
    }

    public static void transparentNavBar(@NonNull final Window window) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) == 0) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
        View decorView = window.getDecorView();
        int vis = decorView.getSystemUiVisibility();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(vis | option);
    }


    public void drawOneSampleChart(DataCache.SampleBean lastSample) {

        LineData lineData = new LineData();
        LineDataSet dataSet;
        List<Entry> entries = new ArrayList<>();
        int size = lastSample.SampleLength;
        for (int i = 0; i < 400; i++) {
            entries.add(new Entry(i, lastSample.Sample[i]));
        }
        dataSet = new LineDataSet(entries, "current");
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.GREEN);
        dataSet.setLineWidth(1);
        dataSet.setValueTextColor(Color.YELLOW);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineData.addDataSet(dataSet);


        oneSampleChart.setData(lineData);

        oneSampleChart.invalidate();

    }
}