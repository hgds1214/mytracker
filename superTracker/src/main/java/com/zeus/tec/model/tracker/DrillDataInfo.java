package com.zeus.tec.model.tracker;

import com.zeus.tec.device.tracker.TrackerCollectData;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class DrillDataInfo {
    @Id
    public long id;

    public long drillInfoId;

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

    public static DrillDataInfo newDrillDataInfo(TrackerCollectData data, long drillInfoId) {
        DrillDataInfo info = new DrillDataInfo();
        info.serialId = data.serialId;
        info.collectTime = data.collectTime;
        info.rollAngle = data.rollAngle;
        info.omega = data.omega;
        info.directionAngle = data.directionAngle;
        info.slantAngle = data.slantAngle;

        info.drillInfoId = drillInfoId;

        return info;
    }
}
