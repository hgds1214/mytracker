package com.zeus.tec.model.tracker;

import com.zeus.tec.device.tracker.TrackerCollectData;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class CollectTimeInfo {
    @Id
    public long id;

    public long drillInfoId;

    public long time;

    public long diffTime = Long.MAX_VALUE;

    //存储点的序号(4B)MSB
    //u32类型
    public long serialId;
    //采集点的时间(4B)MSB
    //u32类型
    public long collectTime;
    // 横滚角(2B)MSB,
    //short类型
    public short rollAngle;
    //俯仰角(2B)MSB,
    //short类型
    public short omega;
    //方位 角(2B)MSB,
    //u16类型
    public int directionAngle;
    //倾斜角(2B)MSB,
    //short类型
    public short slantAngle;

    public void copyData(TrackerCollectData data) {
        this.serialId = data.serialId;
        this.collectTime = data.collectTime;
        this.rollAngle = data.rollAngle;
        this.omega = data.omega;
        this.directionAngle = data.directionAngle;
        this.slantAngle = data.slantAngle;
    }

    @Transient
    public int countId;
}
