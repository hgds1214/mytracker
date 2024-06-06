package com.zeus.tec.model.leida;

import java.time.LocalDateTime;

public class DrillPipe {
    public LocalDateTime StartTime;
    public LocalDateTime EndTime;
    public int IndexFrom;
    public int IndexTo;
    public int ValidCount;
    public float X;
    public float Y;
    public float Z;

    public DrillPipe (LocalDateTime startTime,LocalDateTime endTime){
        this.StartTime = startTime;
        this.EndTime = endTime;
    }
}
