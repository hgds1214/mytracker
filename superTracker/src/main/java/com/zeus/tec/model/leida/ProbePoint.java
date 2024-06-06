package com.zeus.tec.model.leida;

import android.os.Build;

import java.time.LocalDateTime;

public class ProbePoint {
    public LocalDateTime SampleTime;
    public float Heading;
    public float Pitch;
    public float Roll;
    public double [] Voltage;
    public Boolean IsValid;
    public float Distance;
    public int OriginalIndex;

    public double GetDataRange()
    {
        double num = 0.0;
        if (this.Voltage != null)
        {
          //  double num2 = this.Voltage.Min();
          //  double num3 = this.Voltage.Max();
          //  num = (Math.Abs(num3) > Math.Abs(num2)) ? Math.Abs(num3) : Math.Abs(num2);
        }
        return num;
    }

    public ProbePoint(int nSampleLength)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.SampleTime = LocalDateTime.now();
        }
        this.Heading = 0f;
        this.Pitch = 0f;
        this.Roll = 0f;
        this.IsValid = true;
        this.Distance = -1f;
        this.OriginalIndex = -1;
        this.Voltage = new double[nSampleLength];
        for (int i = 0; i < nSampleLength; i++)
        {
            this.Voltage[i] = 0f;
        }
    }

    public ProbePoint(ProbePoint objPoint)
    {
        int length = objPoint.Voltage.length;
        this.SampleTime = objPoint.SampleTime;
        this.Heading = objPoint.Heading;
        this.Pitch = objPoint.Pitch;
        this.Roll = objPoint.Roll;
        this.IsValid = objPoint.IsValid;
        this.Distance = objPoint.Distance;
        this.OriginalIndex = objPoint.OriginalIndex;
        this.Voltage = new double[length];
        for (int i = 0; i < length; i++)
        {
            this.Voltage[i] = objPoint.Voltage[i];
        }
    }

}
