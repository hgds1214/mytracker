package com.zeus.tec.model.leida;

import android.graphics.Canvas;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MergeCache {

    public static float[] SpacingArray = new float[] { 50f, 100f, 200f, 500f };//mm
    public static float PipeLength = 1000f;//mm
    public static int PipeCount =0;
    public static int StartNumber = 1;
  //  public  static  short SampleLength;
    public static List<ProbePoint> lstPointsOrder =new ArrayList<>();
    private static List<LocalDateTime> pointRecordList = new ArrayList<>();
    public static List<DrillPipe> DrillPipeList = new ArrayList<>();
    public static List<ProbePoint> probePointList = new ArrayList<>();
    public static float SpaceSapmle;
    public static float sampleSpeed = 100f;//100000m/s
    public static int Merge_Success = 0x01;
    public static int Merge_Fail = 0x02;

    public static double [][] originData ;

    public static DataHeader dataHeader = new DataHeader() ;

    public static List<double[]> currentColorMap = new ArrayList<>();

    public static void init (){
         lstPointsOrder =new ArrayList<>();
        pointRecordList = new ArrayList<>();
        DrillPipeList = new ArrayList<>();
         probePointList = new ArrayList<>();
        dataHeader = new DataHeader() ;
    }


    public static float GetDefaultSpacing(int count){

        float num = SpacingArray[0];
        for (float v : SpacingArray) {
            num = v;
            int num2 = (int) Math.ceil((double) ((count * PipeLength) / num));
            if (num2 <= 0x7d0) {
                return num;
            }
        }
        return num;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int TimeMatching(List<DrillPipe> lstDrillPipes, List<ProbePoint> lstPointsAll)
    {
        int num = 0;
        int num2 ;
        int count = lstDrillPipes.size();
        int num4 = lstPointsAll.size();
        DrillPipe pipe ;
        ProbePoint point ;
        int num5 = 0;
        for (int i = 0; i < count; i++)
        {
            pipe = lstDrillPipes.get(i);
            pipe.IndexFrom = num5;
            pipe.IndexTo = num4 - 1;
            num2 = 0;
            for (int j = num5; j < num4; j++)
            {
                point = lstPointsAll.get(j);
                int startCompareResult = point.SampleTime.compareTo(pipe.StartTime);
                if (startCompareResult<0)
                {
                    pipe.IndexFrom = j + 1;
                }
                else
                {
                    if (point.IsValid)
                    {
                        num2++;
                    }
                    int endCompareResult = point.SampleTime.compareTo(pipe.EndTime);
                    if (endCompareResult>=0)
                    {
                        pipe.IndexTo = j - 1;
                        if (point.IsValid)
                        {
                            num2--;
                        }
                        break;
                    }
                }
            }
            if (pipe.IndexTo <= pipe.IndexFrom)
            {
                num++;
            }
            num5 = pipe.IndexTo + 1;
            pipe.ValidCount = num2;
        }
        return num;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int OrganizeList(List<DrillPipe> lstDrillPipes, List<ProbePoint> lstPointsAll,  float fRodLength, float fPointSpacing, int nStep)
    {
        int startNumber = StartNumber;
        int num2 = 0;
        int num3 ;
        int count = lstDrillPipes.size();
        int num5 = lstPointsAll.size();
        LocalDateTime endTime = lstDrillPipes.get(count - 1).EndTime;
        DrillPipe pipe ;
        ProbePoint objPoint;
        ProbePoint item;
        lstPointsOrder.clear();
        if (nStep > 0)
        {
            for (int j = 0; j < num5; j++)
            {
                objPoint = lstPointsAll.get(j);
                objPoint.Distance = -1f;
                if ((objPoint.IsValid && (objPoint.SampleTime.compareTo(endTime)>0 )) && (j >= (num2 * nStep)))
                {
                    objPoint.Distance = num2 + startNumber;
                    item = new ProbePoint(objPoint);
                    lstPointsOrder.add(item);
                    num2++;
                }
            }
            return num2;
        }
        for (int i = 0; i < count; i++)
        {
            pipe = lstDrillPipes.get(i);
            num3 = 0;
            if (pipe.ValidCount > 0)
            {
                for (int j = pipe.IndexFrom; j <= pipe.IndexTo; j++)
                {
                    objPoint = lstPointsAll.get(j);
                    objPoint.Distance = -1f;
                    if (objPoint.IsValid)
                    {
                        num3++;
                        while (((num3 * fRodLength) / ((float) pipe.ValidCount)) >= (((num2 + startNumber) * fPointSpacing) - (i * fRodLength)))
                        {
                            objPoint.Distance = ((num2 + startNumber) * fPointSpacing) / 1000f;
                            item = new ProbePoint(objPoint);
                            lstPointsOrder.add(item);
                            num2++;
                        }
                    }
                }
            }
        }
        originData = new double[num2][];
        for (int i = 0; i < num2; i++) {
           originData[i] = lstPointsOrder.get(i).Voltage;
        }

        return num2;
    }

}
