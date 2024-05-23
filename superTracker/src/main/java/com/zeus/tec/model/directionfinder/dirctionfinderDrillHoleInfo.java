package com.zeus.tec.model.directionfinder;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class dirctionfinderDrillHoleInfo implements Serializable {
    @Id
    public  long id;
    // 企业编号
    public String projectName = "";
    // 矿区编号
    public String miningAreaId = "";
    // 工作面名称
    public String workspaceName = "";
    // 钻孔编号
    public String drillHoleId = "";
    // 检测人员
    public String detector = "";
    // 罗盘校准误差 u16
    public int compassCalibrationError;
    // 预设航向
    public int presetHeading;
    // 激光水平误差
    public long laserLevelError;

    // 孔径坐标 X
    public double holeX;
    // 孔径坐标 Y
    public double holeY;
    // 孔径坐标 Z
    public double holeZ;
    // 钻孔设计深度 mm
    public long jacketLength;
    // 设计方位 u16 设计方位角??
    public int designDirection;
    // 设计倾角 s16
    public short designAngle;
    // 倾角修正角 s16 ??
    public short angleAdjustValue;
    // 设计方位角度 u16 ??
    public int designDirectionAngle;
    // 方位角修正 s16 ??
    public short directionAngleAdjustValue;
    // 设计深度 u32 3 bytes ??
    public long designLength;
    // 测量间距 u16 ??
    public int measureSpacing;
    // 设备ID u32 3 bytes ??
    public long deviceID;

    // 校准模式
    public String adjustMode = "";
    // 现场照片
    public String livePhotos;
    // 现场照片MD5
    public String livePhotosMd5;
    // 采集时间
    public long collectionDateTime;

    // 是否数据合成
    public boolean isMerged;
    // 文件父目录，底下存放图片、数据文件、压缩文件
    public String projectRoot;
    // 数据文件
    public String dataPath;
    // 压缩文件
    public String zipPath;
    // 采集运行时长
    public long countTimeTotal;
    // 采集点数
    public long collectCount;

    @Override
    public String toString() {
        return "DrillHoleInfo{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", miningAreaId='" + miningAreaId + '\'' +
                ", workspaceName='" + workspaceName + '\'' +
                ", drillHoleId='" + drillHoleId + '\'' +
                ", detector='" + detector + '\'' +
                ", compassCalibrationError=" + compassCalibrationError +
                ", presetHeading=" + presetHeading +
                ", laserLevelError=" + laserLevelError +
                ", holeX=" + holeX +
                ", holeY=" + holeY +
                ", holeZ=" + holeZ +
                ", jacketLength=" + jacketLength +
                ", designDirection=" + designDirection +
                ", designAngle=" + designAngle +
                ", angleAdjustValue=" + angleAdjustValue +
                ", designDirectionAngle=" + designDirectionAngle +
                ", directionAngleAdjustValue=" + directionAngleAdjustValue +
                ", designLength=" + designLength +
                ", measureSpacing=" + measureSpacing +
                ", deviceID=" + deviceID +
                ", adjustMode='" + adjustMode + '\'' +
                ", livePhotos='" + livePhotos + '\'' +
                ", livePhotosMd5='" + livePhotosMd5 + '\'' +
                ", collectionDateTime=" + collectionDateTime +
                ", isMerged=" + isMerged +
                ", projectRoot='" + projectRoot + '\'' +
                ", dataPath='" + dataPath + '\'' +
                ", zipPath='" + zipPath + '\'' +
                '}';
    }

}
