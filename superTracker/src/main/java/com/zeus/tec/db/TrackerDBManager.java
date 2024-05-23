package com.zeus.tec.db;

import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo;
import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo_;
import com.zeus.tec.model.directionfinder.directionfinderPointRecordInfo;
import com.zeus.tec.model.directionfinder.directionfinderPointRecordInfo_;
import com.zeus.tec.model.leida.leidaPointRecordInfo;
import com.zeus.tec.model.leida.leidaPointRecordInfo_;
import com.zeus.tec.model.leida.leida_info;
import com.zeus.tec.model.leida.leida_info_;
import com.zeus.tec.model.tracker.CollectTimeInfo;
import com.zeus.tec.model.tracker.CollectTimeInfo;
//import com.zeus.tec.model.tracker.CollectTimeInfo_;
import com.zeus.tec.model.tracker.CollectTimeInfo_;
import com.zeus.tec.model.tracker.DrillDataInfo;
import com.zeus.tec.model.tracker.DrillDataInfo_;
import com.zeus.tec.model.tracker.DrillHoleInfo;
import com.zeus.tec.model.tracker.DrillHoleInfo_;
import com.zeus.tec.model.tracker.LastDrillHoleInfo;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 * Created by AllenWang on 2022/8/9.
 */
public class TrackerDBManager {

    /*public static LastDrillHoleInfo getLastDrillHoleInfo() {
        Box<LastDrillHoleInfo> box = ObjectBox.get().boxFor(LastDrillHoleInfo.class);
        LastDrillHoleInfo first = box.query().build().findFirst();
        if (first == null) {
            first = new LastDrillHoleInfo();
            first.id = box.put(first);
        }
        return first;
    }*/

    public static CollectTimeInfo savOrUpdate(CollectTimeInfo collectTimeInfo) {
        Box<CollectTimeInfo> box = ObjectBox.get().boxFor(CollectTimeInfo.class);
        collectTimeInfo.id = box.put(collectTimeInfo);
        return collectTimeInfo;
    }

    // region DrillHoleInfo

    public static long saveOrUpdate(DrillHoleInfo info) {
        return ObjectBox.get().boxFor(DrillHoleInfo.class).put(info);
    }

    public static void saveOrUpdate(DrillDataInfo info) {
        ObjectBox.get().boxFor(DrillDataInfo.class).put(info);
    }
    public static List<CollectTimeInfo> getTimeList(long drillId) {
        return ObjectBox.get().boxFor(CollectTimeInfo.class)
                .query().equal(CollectTimeInfo_.drillInfoId, drillId)
                .order(CollectTimeInfo_.time)
                .build()
                .find();
    }

    public static DrillHoleInfo getOneDrillHoleInfo(long id) {
        return ObjectBox.get().boxFor(DrillHoleInfo.class)
                .get(id);
    }

    public static DrillHoleInfo getLastDrillHoleInfo() {
        Box<DrillHoleInfo> box = ObjectBox.get().boxFor(DrillHoleInfo.class);
        return box.query()
                .orderDesc(DrillHoleInfo_.collectionDateTime)
                .build()
                .findFirst();
    }

    //endregion

    //region tsp 20231212 dirctionfinderDrillHoleInfo
    public static  long  saveOrUpdate(dirctionfinderDrillHoleInfo dirctionfinderDrillHoleInfo){
        return ObjectBox.get().boxFor(dirctionfinderDrillHoleInfo.class).put(dirctionfinderDrillHoleInfo);
    }

    public static  dirctionfinderDrillHoleInfo getLastDirectionfinderDrillHoleInfo(){
        Box<dirctionfinderDrillHoleInfo> box = ObjectBox.get().boxFor(dirctionfinderDrillHoleInfo.class);
        return box.query()
                .orderDesc(dirctionfinderDrillHoleInfo_.collectionDateTime)
                .build()
                .findFirst();
    }

    public static dirctionfinderDrillHoleInfo getOnedirctionfinderDrillHoleInfo(long id) {
        return ObjectBox.get().boxFor(dirctionfinderDrillHoleInfo.class)
                .get(id);
    }

   // endregion

    // region leidainfo
    public  static  long saveOrUpdate(leida_info leidaInfo ){
        return  ObjectBox.get().boxFor(leida_info.class).put(leidaInfo);
    }

    public static leida_info  getLastLeidaInfo (){
        Box<leida_info> box = ObjectBox.get().boxFor((leida_info.class));
        return  box.query()
                .orderDesc(leida_info_.id)
                .build()
                .findFirst();
    }

    public  static  leida_info getOneLeidaInfo (long id){
        return ObjectBox.get().boxFor(leida_info.class)
                .get(id);
    }

    public  static  List<leida_info> isHaveData (String leidaProjectName){
        return   ObjectBox.get().boxFor(leida_info.class)
                .query().equal(leida_info_.projectId,leidaProjectName, QueryBuilder.StringOrder.CASE_SENSITIVE)
                 .build()
                 .find();
//          if (leidaInfo1.size()==0){
//              return false;
//          }
//          else {
//              return true;
//          }

    }
    // endregion


    //region directionfinderPointrecordinfo

    public static  long saveOrUpdate(directionfinderPointRecordInfo recordInfo){
        return ObjectBox.get().boxFor(directionfinderPointRecordInfo.class).put(recordInfo);
    }

    public static directionfinderPointRecordInfo getOnerecordInfo(long id) {
        return ObjectBox.get().boxFor(directionfinderPointRecordInfo.class)
                .get(id);
    }

    public  static List<directionfinderPointRecordInfo> getrecordBydirectionfinderInfoId (long drillId){
        return ObjectBox.get().boxFor(directionfinderPointRecordInfo.class)
                .query().equal(directionfinderPointRecordInfo_.directionfinderInfoId,drillId)
                .build()
                .find();
    }

    //endregion

    //region  leidaPointRecordInfo
    public static  long saveOrUpdate(leidaPointRecordInfo recordInfo){
        return ObjectBox.get().boxFor(leidaPointRecordInfo.class).put(recordInfo);
    }

    public static leidaPointRecordInfo getOneleidarecordInfo(long id) {
        return ObjectBox.get().boxFor(leidaPointRecordInfo.class)
                .get(id);
    }

    public  static List<leidaPointRecordInfo> getrecordByleidaInfoId (long leidaid){
        return ObjectBox.get().boxFor(leidaPointRecordInfo.class)
                .query().equal(leidaPointRecordInfo_.leidaInfoId,leidaid)
                .build()
                .find();
    }



    //endregion


}
