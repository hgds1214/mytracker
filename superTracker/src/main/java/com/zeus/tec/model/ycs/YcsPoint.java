package com.zeus.tec.model.ycs;

public class YcsPoint {

    public YcsPoint(int number, String time, float distance) {
        this.number = number;
        this.time = time;
        this.distance = distance;
    }

    public YcsPoint(int number, String time, float distance,String timeCode) {
        this.number = number;
        this.time = time;
        this.distance = distance;
        this.timeCode = timeCode;
    }

    public int number ;
    public String time ;
    public float distance;
    public String timeCode;
}
