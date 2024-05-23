package com.zeus.tec.model.directionfinder;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class directionfinderPointRecordInfo implements Serializable {
    @Id
    public long id;

    //directionfinderInfoId (外键)
    public long directionfinderInfoId;
    //测点编号
    public int recordId;
    //测点时间
    public  String recordTime;
    //测点方位角
    public  double oritentionAngle;
    //测点倾斜角
    public  double dipAngle;
    //测点相对方位角
    public  double relativeAngle;



}
