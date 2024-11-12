package com.zeus.tec.ui.tracker.util;

import com.zeus.tec.db.TrackerDBManager;
import com.zeus.tec.model.directionfinder.dirctionfinderDrillHoleInfo;
import com.zeus.tec.model.tracker.DrillHoleInfo;

public class ProjectInfoManager {
    private static ProjectInfoManager instance = new ProjectInfoManager();
    private DrillHoleInfo drillHoleInfo;
    public dirctionfinderDrillHoleInfo dirctionfinderDrillHoleInfo;
    public int magnetic_value = 0;

    public static ProjectInfoManager getInstance() {
        return instance;
    }
    public dirctionfinderDrillHoleInfo getOrNewDirctionfinderDrillHoleInfo(){
        return getOrNewdirctionfinderDrillHoleInfo(false);
    }

    public DrillHoleInfo getOrNewDrillHoleInfo() {
        return getOrNewDrillHoleInfo(false);
    }

    public  dirctionfinderDrillHoleInfo getOrNewdirctionfinderDrillHoleInfo(boolean needReset){
        if (dirctionfinderDrillHoleInfo ==null){
            dirctionfinderDrillHoleInfo = TrackerDBManager.getLastDirectionfinderDrillHoleInfo();
        }
        if (dirctionfinderDrillHoleInfo==null){
            dirctionfinderDrillHoleInfo = new dirctionfinderDrillHoleInfo();
        }
        if (needReset){
            dirctionfinderDrillHoleInfo.id =0;
            dirctionfinderDrillHoleInfo.collectionDateTime = 0;
            dirctionfinderDrillHoleInfo.isMerged = false;
            dirctionfinderDrillHoleInfo.livePhotos = "";
            dirctionfinderDrillHoleInfo.livePhotosMd5 = "";
            dirctionfinderDrillHoleInfo.zipPath = "";
            dirctionfinderDrillHoleInfo.projectRoot = "";
            dirctionfinderDrillHoleInfo.dataPath = "";
            dirctionfinderDrillHoleInfo.collectCount = 0;
            dirctionfinderDrillHoleInfo.countTimeTotal = 0;
        }
        return  dirctionfinderDrillHoleInfo;
    }

    public DrillHoleInfo getOrNewDrillHoleInfo(boolean needReset) {
        if (drillHoleInfo == null) {
            drillHoleInfo = TrackerDBManager.getLastDrillHoleInfo();
        }
        if (drillHoleInfo == null) {
            drillHoleInfo = new DrillHoleInfo();
        }
        if (needReset) {
            drillHoleInfo.id = 0;
            drillHoleInfo.collectionDateTime = 0;
            drillHoleInfo.isMerged = false;
            drillHoleInfo.livePhotos = "";
            drillHoleInfo.livePhotosMd5 = "";
            drillHoleInfo.zipPath = "";
            drillHoleInfo.projectRoot = "";
            drillHoleInfo.dataPath = "";
            drillHoleInfo.collectCount = 0;
            drillHoleInfo.countTimeTotal = 0;
        }
        return drillHoleInfo;
    }

    public void updateDrillInfo(DrillHoleInfo info) {
        drillHoleInfo = info;
        if(drillHoleInfo != null) {
            drillHoleInfo.id = 0;
            drillHoleInfo.collectionDateTime = 0;
        }
    }
}
