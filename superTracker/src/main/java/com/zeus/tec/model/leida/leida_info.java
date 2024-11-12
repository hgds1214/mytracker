package com.zeus.tec.model.leida;

import com.zeus.tec.model.leida.main.MainCache;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
@Entity
public class leida_info implements Serializable {
    @Id
    public long id;
    private static leida_info _instance = new leida_info();
    public static leida_info GetInstance()
    {
        if (_instance == null)
        {
            _instance = new leida_info();

        }
        return _instance;
    }
    public  static  void setInstance (leida_info leidaInfo){
       _instance = leidaInfo;
    }
    public String creatTime = "";
    // 企业编号
    public String projectId = "";
    // 采样长度：0为512，1为1024，2为2048
    public int sampleLength = 1;
   //public int [] SpLength = {512,1024,2048};
    // 采样频率 0为1000，1为2000，2为4000
    public int frequency = 1;
    //public int [] FR = {1000,2000,4000};
    // 放大倍数
    public int  Amp1 =1;
    // 延迟点数
    public int  Delay1 = 1 ;
    // 叠加次数
    public int overlaynumbe = 32;
    // 打点距离（钻杆长度）
    public float drillPipeLength = 100.0f;
    // 时间间隔
    public float timeSpace = 50;

    //陀螺阈值
    public float GYROThreshold = 40;

    //下载地址
    public String dataDownload = "";

    //采集点数
    public int PointCount=0;

    //采集深度
    public float TotalDis =0;

//    // 孔径坐标 X
//    public double holeX;
//    // 孔径坐标 Y
//    public double holeY;
//    // 孔径坐标 Z
//    public double holeZ;
//    // 护套长度 mm
//    public long jacketLength;
//    // 设计方位 u16 设计方位角??
//    public int designDirection;
//    // 设计倾角 s16
//    public short designAngle;
//    // 倾角修正角 s16 ??
//    public short angleAdjustValue;
//    // 设计方位角度 u16 ??
//    public int designDirectionAngle;
//    // 方位角修正 s16 ??
//    public short directionAngleAdjustValue;
//    // 设计深度 u32 3 bytes ??
//    public long designLength;

    // 测量间距 u16 ??

 //   public int measureSpacing;
    // 设备ID u32 3 bytes ??

    public long deviceID;
//
//    // 校准模式
//    public String adjustMode = "";
//    // 现场照片
//    public String livePhotos;
//    // 现场照片MD5
//    public String livePhotosMd5;
    // 采集时间
  //  public long collectionDateTime;

    // 是否数据合成
    public boolean isMerged;

    // 文件父目录，底下存放图片、数据文件、压缩文件
    public String projectRoot;
    // 数据文件
    public String dataPath;
    // 压缩文件
    public String zipPath;
    // 采集运行时长
  //  public long countTimeTotal;
    // 采集点数
 //   public long collectCount;

  //  public int currentPoint=0;

  //  public float currentDepth=0;


}
