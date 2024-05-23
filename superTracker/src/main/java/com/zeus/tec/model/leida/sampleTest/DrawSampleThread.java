package com.zeus.tec.model.leida.sampleTest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zeus.tec.R;

import java.util.ArrayList;
import java.util.List;

public class DrawSampleThread {

    private volatile Boolean stop = false;
    DataCache cache = DataCache.GetInstance();
    int index_list = 0;
    LineChart lineChart;
    Context context;

    public DrawSampleThread(LineChart Linechart , Context Context) {
        this.lineChart = Linechart;
        this.context = Context;
    }

    //public delegate void UpdateMainPanel();
    // public UpdateMainPanel UpdateMainPanelDelegate;

    public void Stop() {
        stop = true;
    }

    public void getEntry(DataCache.SampleBean sampleBean) {
        List<Entry> entries = new ArrayList<Entry>();
//        List<Entry> entries1 = new ArrayList<Entry>();
//        List<Entry> entries2 = new ArrayList<Entry>();
//        List<Entry> entries3 = new ArrayList<Entry>();
        for (int i = 0; i < 512; i++) {
            entries.add(new Entry(1 * i, sampleBean.Sample[i]));
        }
//        for (int i = 0; i < 512; i++) {
//            entries1.add(new Entry(1 * i, sampleBean.Sample[i] + 2f));
//        }
//        for (int i = 0; i < 512; i++) {
//            entries2.add(new Entry(1 * i, sampleBean.Sample[i] + 4f));
//        }
//        for (int i = 0; i < 512; i++) {
//            entries3.add(new Entry(1 * i, sampleBean.Sample[i] + 6f));
//        }
        LineDataSet dataSet = new LineDataSet(entries, "Test");
//        LineDataSet dataSet1 = new LineDataSet(entries1, "Test1");
//        LineDataSet dataSet2 = new LineDataSet(entries2, "Test2");
//        LineDataSet dataSet3 = new LineDataSet(entries3, "Test3");
//        dataSet1.setColor(Color.GREEN);
//        dataSet1.setDrawCircles(false);
//        dataSet2.setColor(Color.YELLOW);
//        dataSet3.setColor(Color.BLUE);
//        dataSet3.setDrawCircles(false);
//        dataSet2.setDrawCircles(false);
        dataSet.setColor(Color.BLACK);
        dataSet.setDrawCircles(false);
        LineData lineData = new LineData();
        // LineData lineData1 = new LineData(dataSet1);
        //  LineChart lineChart = getView().findViewById(R.id.chart1);
        lineData.addDataSet(dataSet);
//        lineData.addDataSet(dataSet1);
//        lineData.addDataSet(dataSet2);
//        lineData.addDataSet(dataSet3);
        ((Activity)context ).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lineChart.setData(lineData);
//           YAxis leftAxis = lineChart.getAxisLeft();
//           leftAxis.setLabelCount(5, true); //
                lineChart.invalidate();
            }
        });
    }

    public void Run() {

        while (!stop) {

            try {

                // DataCache.SampleBean sample = cache.SampleQue.Dequeue();
                if (cache.SampleQue.size()>=1){
                    DataCache.SampleBean sample = cache.SampleQue.get(cache.SampleQue.size()-1);
                    cache.LastSample = sample;
                }
                int CurrentImageNum = cache.ImageList.size();
                if (cache.LastSample !=null){

                            getEntry(cache.LastSample);

                }

                //  Image image = CreateSampleImage(cache.SampleWidth, cache.SampleHeight, sample, false);
//                if (cache.LastImage != null)
//                {
//                    Graphics bg = Graphics.FromImage(cache.LastImage);
//                    bg.Clear(Color.White);
//                }
//                else
//                {
//                    cache.LastImage = new Bitmap(cache.LastWidth, cache.LastHeight);
//                }

                // CreateCurrentSampleImage(cache.LastImage, cache.LastWidth, cache.LastHeight, sample, true);
//                if (image != null)
//                {
//                    cache.ImageList.Add(image);
//                    int ClearNum = CurrentImageNum - cache.ShowSampleNum;
//                    if (ClearNum > 0)
//                        cache.ImageList.RemoveRange(0, ClearNum);
//
//                    if (cache.MainImage != null)
//                    {
//                        int Num = cache.ImageList.Count;
//                        Graphics bg = Graphics.FromImage(cache.MainImage);
//                        bg.Clear(Color.White);
//
//                        for (int i = 0; i < cache.ImageList.Count; i++)
//                        {
//                            Image bt = cache.ImageList[i];
//                            Num--;
//                            int x = Num * bt.Width;
//                            int y = 0;
//                            if (bt != null)
//                            {
//                                bg.DrawImage(bt, x, y, bt.Width, bt.Height);
//                            }
//                        }
//
//                        UpdateMainPanelDelegate();
//                    }
//
//                }

            } catch (Exception exception)
            {
                exception.printStackTrace();
                continue;
            }

        }

    }
}
