package com.zeus.tec.model.leida;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class leidaPointRecordInfo implements Serializable {
    @Id
    public long id;

    public long leidaInfoId ;

    public  String PointNumber;

    public  String recordTime;

    public  String distance ;





}
